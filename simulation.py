#!/usr/bin/env python

"""
A script to simulate herb farming yield. This is used to check the output of
the calculator, as a safety check. Not used in production.
"""

import argparse
import json
import math
import random
from prettytable import PrettyTable
from tqdm import tqdm
from typing import NamedTuple

PATCH_ARDOUGNE = "ardougne"
PATCH_CATHERBY = "catherby"
PATCH_FALADOR = "falador"
PATCH_FARMING_GUILD = "farming_guild"
PATCH_HARMONY = "harmony"
PATCH_HOSIDIUS = "hosidius"
PATCH_PORT_PHASMATYS = "port_phasmatys"
PATCH_TROLL_STRONGHOLD = "troll_stronghold"
PATCH_WEISS = "weiss"

COMPOST_NORMAL = "compost"
COMPOST_SUPER = "supercompost"
COMPOST_ULTRA = "ultracompost"

DIARY_EASY = "easy"
DIARY_MEDIUM = "medium"
DIARY_HARD = "hard"
DIARY_ELITE = "elite"

ANIMA_ATTAS = "attas"
ANIMA_IASOR = "iasor"
ANIMA_KRONOS = "kronos"


"""
An herb. `min_chance_to_save` is the "chance to save a life while harvesting"
for the herb at level 1. All herbs have a base chance to save of 80% at level
99, and for each herb we linearly interpolate between the two based on the
player's farming level.

Values ripped from:
https://oldschool.runescape.wiki/w/Calculator:Farming/Herbs/Template?action=edit
"""
Herb = NamedTuple(
    "Herb",
    [
        ("name", str),
        ("level", int),
        ("level_1_chance_to_save", float),
        ("seed_xp", float),
        ("harvest_xp", float),
    ],
)

HERBS = [
    Herb("Guam", 9, 25.0, 11.0, 12.5),
    Herb("Marrentill", 14, 28.0, 13.5, 15.0),
    Herb("Tarromin", 19, 31.0, 16.0, 18.0),
    Herb("Harralander", 26, 36.0, 21.5, 24.0),
    Herb("Ranarr", 32, 39.0, 27.0, 30.5),
    Herb("Toadflax", 38, 43.0, 34.0, 38.5),
    Herb("Irit", 44, 46.0, 43.0, 48.5),
    Herb("Avantoe", 50, 50.0, 54.5, 61.5),
    Herb("Kwuarm", 56, 54.0, 69.0, 78.0),
    Herb("Snapdragon", 62, 57.0, 87.5, 98.5),
    Herb("Cadantine", 67, 60.0, 106.5, 120.0),
    Herb("Lantadyme", 73, 64.0, 134.5, 151.5),
    Herb("Dwarf Weed", 79, 67.0, 170.5, 192.0),
    Herb("Torstol", 85, 71.0, 199.5, 224.5),
]


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("config_path")
    parser.add_argument("--trials", "-t", type=int, default=1000)
    args = parser.parse_args()

    with open(args.config_path) as f:
        config = json.load(f)

    patch_names = config.pop("patches")
    farming_level = config["farming_level"]
    # Matrix of patch instances; each row is one herb and each column is a patch
    patches_matrix = [
        (
            herb,
            [
                HerbPatch(name=patch_name, herb=herb, **config)
                for patch_name in patch_names
            ],
        )
        for herb in HERBS
        # Only show herbs we have the level to grow
        if herb.level <= farming_level
    ]

    # Total number of trials to run, used to show progress to the user
    pbar = tqdm(total=len(HERBS) * len(patch_names) * args.trials)
    # For each cell in the matrix, run n trials and store the aggregate result
    results = [
        (herb, [patch.simulate(args.trials, pbar) for patch in patches])
        for (herb, patches) in patches_matrix
    ]
    pbar.close()

    def print_table(name, field_name, formatter, average=False):
        table = PrettyTable()
        table.field_names = ["Herb", *patch_names, "Total"]

        for (herb, row_results) in results:
            values = [getattr(cell, field_name) for cell in row_results]
            total = sum(values) / len(values) if average else sum(values)
            table.add_row(
                [
                    herb.name,
                    *(formatter(val) for val in values),
                    formatter(total),
                ]
            )

        print(name)
        print(table)
        print("")

    print_table(
        "Survival Rate",
        "survival_rate",
        lambda survival_rate: f"{survival_rate * 100.0:.1f}%",
        average=True,
    )
    print_table(
        "Yield",
        "herbs_harvested",
        lambda herbs_harvested: f"{herbs_harvested:.1f}",
    )
    print_table("XP", "xp_gained", lambda xp_gained: f"{xp_gained:.1f}")


class HerbPatch:
    def __init__(
        self,
        herb,
        name,
        farming_level,
        compost,
        magic_secateurs,
        farming_cape,
        anima_plant,
        falador_diary,
        kandarin_diary,
        kourend_diary,
        hosidius_fifty_favor,
    ):
        self.name = name
        self.herb = herb
        self.farming_level = farming_level
        self.compost = compost
        self.magic_secateurs = magic_secateurs
        self.farming_cape = farming_cape
        self.anima_plant = anima_plant
        self.falador_diary = falador_diary
        self.kandarin_diary = kandarin_diary
        self.kourend_diary = kourend_diary
        self.hosidius_fifty_favor = hosidius_fifty_favor

    def get_disease_chance_per_cycle(self):
        # Check for disease-free patches first
        if self.name in [PATCH_TROLL_STRONGHOLD, PATCH_WEISS] or (
            self.name == PATCH_HOSIDIUS and self.hosidius_fifty_favor
        ):
            return 0.0

        # https://oldschool.runescape.wiki/w/Disease_(Farming)#Reducing_disease_risk
        # The disease chance is always expressed as a *proper* fraction of 128,
        # i.e. the numerator is always a whole number. So we'll calculate the
        # numerator separately, then divide at the end

        if self.compost is None:
            base_chance = 27.0
        if self.compost == COMPOST_NORMAL:
            base_chance = 14.0
        if self.compost == COMPOST_SUPER:
            base_chance = 6.0
        if self.compost == COMPOST_ULTRA:
            base_chance = 3.0

        modifier = 1.0
        if self.anima_plant == ANIMA_IASOR:
            modifier = 0.2

        # Round *down* to the nearest multiple of 1/128, but not to zero
        numerator = max(math.floor(base_chance * modifier), 1.0)
        return numerator / 128.0

    def get_initial_lives(self):
        if self.compost == COMPOST_ULTRA:
            return 6
        if self.compost == COMPOST_SUPER:
            return 5
        if self.compost == COMPOST_NORMAL:
            return 4
        if self.compost is None:
            return 3
        raise ValueError(f"Invalid compost value: {self.compost}")

    def get_chance_to_save(self):
        item_bonus = self.get_item_chance_to_save_bonus()
        diary_bonus = self.get_diary_chance_to_save_bonus()
        attas_bonus = self.get_attas_chance_to_save_bonus()

        # YES, these chances really are supposed to be this big, they're
        # essentially out of 98, not 1.0
        chance1 = self.herb.level_1_chance_to_save
        chance99 = 80.0  # Max chance to save for all herbs

        # This is easier to read in the Wiki's format:
        # https://oldschool.runescape.wiki/w/Farming#Variable_crop_yield
        # That page doesn't mention anything about the `floor`s though, but it's
        # in the Wiki calculator source code
        # https://oldschool.runescape.wiki/w/Calculator:Template/Farming/Herbs2?action=edit
        return (
            math.floor(
                math.floor(
                    math.floor(
                        (chance1 * (99.0 - self.farming_level) / 98.0)
                        + (chance99 * (self.farming_level - 1.0) / 98.0)
                    )
                    * (1.0 + item_bonus)
                    # https://twitter.com/JagexAsh/status/956892754096869376
                    * (1.0 + diary_bonus)
                    # https://twitter.com/JagexAsh/status/1245644766328446976
                    # Note: This conflicts with how the wiki calculator does it,
                    # but I'm going off of Ash's tweets instead
                    * (1.0 + attas_bonus)
                    + 1.0
                )
            )
            / 256.0
        )

    def get_item_chance_to_save_bonus(self):
        """
        Get the "chance to save bonus" due to equipped items
        """
        bonus = 0.0
        if self.magic_secateurs:
            bonus += 0.1
        if self.farming_cape:
            bonus += 0.05
        return bonus

    def get_diary_chance_to_save_bonus(self):
        """
        Get the "chance to save" bonus *local to this patch*, i.e. not including
        global buffs.
        """
        # Catherby bonus scales based on tier completed
        if self.name == PATCH_CATHERBY:
            if self.kandarin_diary == DIARY_MEDIUM:
                return 0.05
            if self.kandarin_diary == DIARY_HARD:
                return 0.10
            if self.kandarin_diary == DIARY_ELITE:
                return 0.15

        # Both Hosidius and Farming Guild get +5% from Kourend hard
        if (
            self.name
            in [
                PATCH_HOSIDIUS,
                PATCH_FARMING_GUILD,
            ]
            and self.kourend_diary in [DIARY_HARD, DIARY_ELITE]
        ):
            return 0.05

        return 0.0  # Sad

    def get_attas_chance_to_save_bonus(self):
        if self.anima_plant == ANIMA_ATTAS:
            return 0.05
        return 0.0

    def get_xp_bonus(self):
        # 10% bonus for fally medium
        if self.name == PATCH_FALADOR and self.falador_diary in [
            DIARY_MEDIUM,
            DIARY_HARD,
            DIARY_ELITE,
        ]:
            return 0.10
        return 0.0

    def get_compost_xp(self):
        if self.compost == COMPOST_NORMAL:
            return 18.0
        if self.compost == COMPOST_SUPER:
            return 26.0
        if self.compost == COMPOST_ULTRA:
            return 36.0
        return 0.0

    def simulate(self, trials, pbar):
        result = AggregateResult()
        for _ in range(trials):
            state = HerbPatchState(
                herb=self.herb,
                compost=self.compost,
                disease_chance_per_cycle=self.get_disease_chance_per_cycle(),
                initial_lives=self.get_initial_lives(),
                chance_to_save=self.get_chance_to_save(),
                xp_bonus=self.get_xp_bonus(),
            )
            state.grow()
            state.harvest()
            result.add_trial(state)

            pbar.update()  # Update progress bar
        return result


class HerbPatchState:

    # There are really n+1 growth stages, but we start at zero, so there are n
    # transitions (fence post problem)
    FINAL_GROWTH_STAGE = 4

    def __init__(
        self,
        herb,
        disease_chance_per_cycle,
        initial_lives,
        chance_to_save,
        compost_xp,
        xp_bonus,
    ):
        # Constants
        self.herb = herb
        self.disease_chance_per_cycle = disease_chance_per_cycle
        self.chance_to_save = chance_to_save
        self.compost_xp = compost_xp
        self.xp_bonus = xp_bonus

        # Internal game state
        self.lives = initial_lives
        self.growth_stage = 0
        self.is_diseased = False
        self.is_dead = False

        # Player outcomes
        self.herbs_harvested = 0
        self.xp_gained = 0

    def grow(self):
        """
        Progress herb growth until either death or adulthood
        """

        def grow_stage():
            # If we try to grow while already diseased, the plant dies *without*
            # progressing to the next state
            if self.is_diseased:
                self.is_dead = True
                return

            # Roll to see if we get diseased
            if rand_bool(self.disease_chance_per_cycle):
                self.is_diseased = True

            self.growth_stage += 1

        # Grow until we hit a terminal stage
        while self.growth_stage < self.FINAL_GROWTH_STAGE and not self.is_dead:
            grow_stage()

    def harvest(self):
        """
        Harvest this patch until exhaustion. If not fully grown, does nothing
        """
        if self.is_dead:
            return

        if self.growth_stage < self.FINAL_GROWTH_STAGE:
            raise ValueError("Cannot harvest plant before fully grown")

        xp_factor = 1.0 + self.xp_bonus

        def harvest_herb():
            self.herbs_harvested += 1
            self.xp_gained += self.herb.harvest_xp * xp_factor
            if not rand_bool(self.chance_to_save):
                self.lives -= 1

        # Compost XP is given when it's spread
        self.xp_gained += self.compost_xp * xp_factor

        # Harvest until we can't
        while self.lives > 0:
            harvest_herb()
        # For some wacky reason, you don't get the "plant" XP until the final
        # harvest, which is important because it means dead herbs don't provide
        # any XP
        # https://oldschool.runescape.wiki/w/Herb_patch/Seeds#cite_ref-3
        self.xp_gained += self.herb.seed_xp * xp_factor


class AggregateResult:
    def __init__(self):
        self._herbs_harvested = 0
        self._xp_gained = 0
        self._num_survived = 0
        self._num_trials = 0

    @property
    def herbs_harvested(self):
        return self._herbs_harvested / self._num_trials

    @property
    def xp_gained(self):
        return self._xp_gained / self._num_trials

    @property
    def survival_rate(self):
        return self._num_survived / self._num_trials

    def add_trial(self, state):
        self._num_trials += 1
        self._herbs_harvested += state.herbs_harvested
        self._xp_gained += state.xp_gained
        if not state.is_dead:
            self._num_survived += 1

    def __str__(self):
        return f"{self.survival_rate * 100.0}% surv. / \
            {self.herbs_harvested} herbs / {self.xp_gained} XP"


def rand_bool(true_weight=0.5):
    return random.random() < true_weight


if __name__ == "__main__":
    main()
