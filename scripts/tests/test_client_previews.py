"""Hundreds of evidence PNGs must not turn the job log into an unbounded image payload."""
import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('client_previews', Path(__file__).resolve().parents[1] / 'preview_client_screenshots.py')
previews = importlib.util.module_from_spec(spec)
spec.loader.exec_module(previews)


class ClientPreviewTests(unittest.TestCase):
    def test_small_galleries_keep_every_preview(self):
        for size in (0, 1, 7, 24):
            with self.subTest(size=size):
                paths = [f'{i:04}.png' for i in reversed(range(size))]
                self.assertEqual(sorted(paths), previews.preview_paths(paths))

    def test_large_gallery_is_bounded_and_includes_both_ends(self):
        paths = [f'{i:04}.png' for i in range(644)]
        selected = previews.preview_paths(paths)
        self.assertEqual(24, len(selected))
        self.assertEqual(len(selected), len(set(selected)))
        self.assertEqual((paths[0], paths[-1]), (selected[0], selected[-1]))
        self.assertTrue(set(selected).issubset(paths))
        self.assertEqual(644, len(paths), 'The full artifact input remains intact')

    def test_single_preview_budget_and_invalid_budget(self):
        self.assertEqual(['a.png'], previews.preview_paths(['b.png', 'a.png'], 1))
        with self.assertRaises(ValueError):
            previews.preview_paths(['a.png'], 0)


if __name__ == '__main__':
    unittest.main()
