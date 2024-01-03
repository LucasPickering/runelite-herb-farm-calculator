package me.lucaspickering.utils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Comparator;


@AllArgsConstructor
@Getter
public enum SortingCriteria {
    Alphabetical("Alphabetical"),
    Level("Level"),
    Profit("Profit"),
    Yield("Yield"),
    XP("XP");

    public final String name;

    public Comparator<HerbResult> getComparator(){
        switch(this){
            case Alphabetical:
                return Comparator.comparing(herbResult -> herbResult.getHerb().getName());
            case Level:
                return Comparator.comparing(herbResult -> herbResult.getHerb().getLevel());
            case Profit:
                return Comparator.comparingDouble(HerbResult::getProfit);
            case Yield:
                return Comparator.comparingDouble(HerbResult::getExpectedYield);
            case XP:
                return Comparator.comparingDouble(HerbResult::getExpectedXp);
        }
        return Comparator.comparing(herbResult -> herbResult.getHerb().getLevel());
    }



}