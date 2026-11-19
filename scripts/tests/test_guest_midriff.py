"""The two new cropped outfits have readable painted detail on the runtime atlas."""
import math
import unittest
from pathlib import Path

from PIL import Image


class GuestMidriffTest(unittest.TestCase):
    def test_navel_and_side_planes_read_against_neighboring_skin(self):
        assets = Path(__file__).resolve().parents[2] / "src/main/resources/assets/jugcraft"
        for guest, hem, curve, waist in (("brittany_venti", 7.25, .16, 9.67),
                                        ("sarah_stock", 7.22, .12, 9.20)):
            with self.subTest(guest=guest), Image.open(
                assets / "textures/entity/town_guests" / f"guest_{guest}.png"
            ) as atlas:
                def green(angle, depth):
                    top = hem + curve * math.cos(2 * angle)
                    y = top + (waist - top) * depth
                    return atlas.getpixel((16 + round((.5 + angle / math.tau) * 739),
                                           1310 + round(y / 24.1 * 699)))[1]

                self.assertGreater(green(.25, .68) - green(.009, .68), 25)
                for side in (-1, 1):
                    self.assertGreater(green(side * .28, .45) - green(side * .57, .45), 6)
