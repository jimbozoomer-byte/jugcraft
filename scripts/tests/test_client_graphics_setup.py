"""Exercise CI mirror recovery without installing packages or using the network."""
import os
from pathlib import Path
import shutil
import subprocess
import unittest


ROOT = Path(__file__).resolve().parents[2]
BASH = shutil.which("bash")
if os.name == "nt" and Path("C:/Program Files/Git/bin/bash.exe").is_file():
    BASH = "C:/Program Files/Git/bin/bash.exe"

# Intercept the privileged command at the shell boundary. Everything else, including
# source generation, branching, exit propagation and cleanup, runs as in CI.
HARNESS = r'''
sudo() {
  [[ "$1 $2 $3 $4" == 'timeout --kill-after=10s 120s apt-get' ]] || return 90
  shift 4
  local args=" $* " source_file='' arg
  [[ "$args" == *' Dir::Etc::sourceparts=- '* ]] || return 91
  [[ "$args" == *' APT::Update::Error-Mode=any '* ]] || return 92
  for arg in "$@"; do
    case "$arg" in Dir::Etc::sourcelist=*) source_file=${arg#*=};; esac
  done
  [[ -f "$source_file" ]] || return 93
  [[ $(wc -l < "$source_file") -eq 4 ]] || return 94
  [[ $(grep -c 'signed-by=/usr/share/keyrings/ubuntu-archive-keyring.gpg' "$source_file") -eq 4 ]] || return 95
  local mirror=primary phase=update
  grep -q 'https://azure.archive.ubuntu.com' "$source_file" && mirror=fallback
  [[ "$args" == *' --download-only '* ]] && phase=download
  [[ "$args" == *' --no-download '* ]] && phase=install
  echo "CALL $mirror $phase"
  if [[ "$phase" != update ]]; then
    [[ "$args" == *' mesa-vulkan-drivers libgl1-mesa-dri libglx-mesa0 libegl1 libegl-mesa0 mesa-utils xvfb '* ]] || return 96
  fi
  case "$CASE:$mirror:$phase" in
    update_failure:primary:update|both_fail:*:update) return 100;;
    download_timeout:primary:download) return 124;;
    install_failure:*:install) return 42;;
  esac
  return 0
}
source scripts/install_client_graphics.sh
'''


@unittest.skipUnless(BASH, "Bash is required for the Ubuntu CI setup script")
class ClientGraphicsSetupTests(unittest.TestCase):
    def run_case(self, case, expected_code, expected_calls):
        result = subprocess.run(
            [BASH, "-c", HARNESS], cwd=ROOT,
            env={**os.environ, "CASE": case}, text=True, capture_output=True, timeout=20,
        )
        self.assertEqual(expected_code, result.returncode, result.stdout + result.stderr)
        self.assertEqual(expected_calls, [line[5:] for line in result.stdout.splitlines() if line.startswith("CALL ")])

    def test_primary_success(self):
        self.run_case("success", 0, ["primary update", "primary download", "primary install"])

    def test_index_failure_uses_fallback(self):
        self.run_case("update_failure", 0, ["primary update", "fallback update", "fallback download", "fallback install"])

    def test_slow_download_uses_fallback(self):
        self.run_case("download_timeout", 0, ["primary update", "primary download", "fallback update", "fallback download", "fallback install"])

    def test_no_healthy_mirror_fails(self):
        self.run_case("both_fail", 1, ["primary update", "fallback update"])

    def test_install_error_is_not_retried_or_hidden(self):
        self.run_case("install_failure", 42, ["primary update", "primary download", "primary install"])


if __name__ == "__main__":
    unittest.main()
