package io.github.talkarcabbage.planksacktracker;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SkillMenuActivationEvent {
    private int scriptEventID;
    private int itemID; //for 2050, this is args[2]; for 2051, this is args[3]
    private int menuID; //For 2050, this is args[3]; For 2051, this is args[4]; 17694734...
}
