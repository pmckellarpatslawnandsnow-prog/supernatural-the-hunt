package com.fanmods.snhunt.story;

import com.fanmods.snhunt.registry.ModBlocks;
import com.fanmods.snhunt.registry.ModEntities;
import com.fanmods.snhunt.registry.ModItems;
import com.fanmods.snhunt.story.Chapter.Objective;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.fanmods.snhunt.story.Chapter.Speaker.BOBBY;
import static com.fanmods.snhunt.story.Chapter.Speaker.DEAN;
import static com.fanmods.snhunt.story.Chapter.Speaker.JOHN;
import static com.fanmods.snhunt.story.Chapter.Speaker.JOURNAL;
import static com.fanmods.snhunt.story.Chapter.Speaker.MEG;
import static com.fanmods.snhunt.story.Chapter.Speaker.MISSOURI;
import static com.fanmods.snhunt.story.Chapter.Speaker.SAM;

/**
 * Season 1 story mode. Each chapter follows an episode of the first season, retold as a Minecraft hunt.
 * You play a young hunter riding along with the Winchester brothers. All dialogue here is original writing
 * for this fan mod, not lines from the show.
 */
public final class Season1 {
    private static List<Chapter> chapters;

    public static List<Chapter> chapters() {
        if (chapters == null) {
            chapters = Collections.unmodifiableList(build());
        }
        return chapters;
    }

    private static List<Chapter> build() {
        List<Chapter> list = new ArrayList<>();
        int n = 0;

        // 1x01
        list.add(new Chapter(++n, "Pilot", "Jericho, California", Objective.SALT_BURN)
                .narrate("Stanford. Sam Winchester has a girlfriend named Jessica, a law school interview on Monday, and a life with no monsters in it.")
                .narrate("At two in the morning, his older brother Dean breaks in through the window.")
                .say(DEAN, "Dad went out on a hunt and stopped checking in. I need you, Sammy. One trip.")
                .say(SAM, "One weekend. I'm back by Monday.")
                .narrate("In Jericho, young men keep vanishing on the same stretch of road. Locals whisper about a woman in a white dress who flags down drivers at night.")
                .say(DEAN, "You're the kid who's been chasing the same story? Fine. Get in the car. Stay behind us.")
                .goal("The Woman in White is a vengeful spirit: weapons pass right through her. Iron and rock salt drive her off for a few seconds. "
                        + "Find her grave, salt it (right-click with Rock Salt) and burn it (Flint and Steel).")
                .spawnBound(ModEntities.SPIRIT, "Woman in White")
                .anchor(ModBlocks.GRAVE, "her grave")
                .outroNarrate("The spirit gives one last scream and goes up with her bones. The road outside Jericho is quiet again.")
                .outroNarrate("John Winchester isn't in Jericho. All he left behind is a motel room full of research, and coordinates pointing somewhere new.")
                .outroNarrate("Dean drops Sam back at Stanford. Minutes later the apartment is on fire. Jessica is killed the same way Sam's mother was, twenty-two years ago.")
                .outroSay(SAM, "Whatever did this to her, I'm going to find it.")
                .outroNarrate("Sam throws his bag in the back of the Impala and doesn't look back.")
                .reward(ModItems.FLARE_GUN, 1)
                .reward(ModItems.SALT, 16));

        // 1x02
        list.add(new Chapter(++n, "Wendigo", "Blackwater Ridge, Colorado", Objective.KILL)
                .narrate("John's coordinates lead to Blackwater Ridge, deep in the Colorado woods.")
                .narrate("A young woman named Haley says her brother Tommy stopped calling from his camping trip. The ranger station has missing hikers on file going back decades.")
                .say(SAM, "Claws, no tracks, and it took the bodies. Dad's journal has a word for that.")
                .say(JOURNAL, "Wendigo. Once human. Fast, smart, always hungry. Bullets won't stop it. Fire will.")
                .say(DEAN, "Great. So we bring fire.")
                .goal("The Wendigo barely feels ordinary weapons. Set it on fire with the Flare Gun, Flint and Steel or fire charges, then hit it while it burns.")
                .spawn(ModEntities.WENDIGO, 1, "Wendigo")
                .outroNarrate("The flare finds its mark and the wendigo burns from the inside out.")
                .outroNarrate("Tommy is alive in the old mine. Haley gets her brother back.")
                .outroSay(DEAN, "No sign of Dad. But we got some people home tonight. That counts for something.")
                .outroSay(SAM, "It doesn't mean I stop looking for him.")
                .reward(ModItems.SALT, 32));

        // 1x03
        list.add(new Chapter(++n, "Dead in the Water", "Lake Manitoc, Wisconsin", Objective.SURVIVE)
                .narrate("Lake Manitoc. Three drownings this year, and every victim drowned somewhere water shouldn't be able to kill anyone: a sink, a bathtub.")
                .say(SAM, "Sheriff Carlton's family keeps losing people. Whatever's in that lake hates him.")
                .narrate("Andrea's little boy Lucas hasn't spoken since his father drowned. Now he draws: a house, a red bicycle, a boy under the water.")
                .say(DEAN, "Kid knows something. We keep him safe until we figure out what.")
                .goal("The spirit of a drowned boy is dragging people into the lake. Hold the dock until the water goes still.")
                .survive(75, 20)
                .spawn(ModEntities.SPIRIT, 1, "Spirit of the Lake")
                .spawn(EntityType.DROWNED, 2, "Lake Thrall")
                .outroNarrate("Thirty-five years ago, a young Bill Carlton and a friend drowned a boy named Peter Sweeney, and the lake kept the secret.")
                .outroNarrate("Bill walks into the water to give Peter what he wants. The spirit takes him under and lets Lucas go.")
                .outroNarrate("That night, Lucas talks to his mother for the first time in weeks.")
                .reward(ModItems.HOLY_WATER, 4)
                .reward(ModItems.DEVILS_TRAP, 4)
                .reward(ModItems.EXORCISM_RITE, 1));

        // 1x04
        list.add(new Chapter(++n, "Phantom Traveler", "Pittsburgh, Pennsylvania", Objective.EXORCISE)
                .narrate("Jerry Panowski, an old friend of John's, calls about a plane crash. The cockpit recording has something on it that isn't static.")
                .say(SAM, "Slow the audio down and there's a voice. It's promising there won't be survivors.")
                .say(DEAN, "A demon's jumping from passenger to passenger and dropping planes. Which means I have to get on one.")
                .narrate("Dean hates flying. Today, he's flying.")
                .goal("A demon can't be killed with ordinary weapons; its human host would die first. Lure it onto a Devil's Trap, then read the Rite of Exorcism nearby. Holy Water burns it.")
                .startWith(ModItems.DEVILS_TRAP, 2)
                .spawn(ModEntities.DEMON, 1, "Possessed Passenger")
                .outroNarrate("With the plane shaking itself apart, Sam reads the rite. Black smoke pours out of the co-pilot and into the vents.")
                .outroNarrate("The plane lands. Dean has never been so happy to see an airport.")
                .outroSay(DEAN, "We're driving from now on. Every time. Forever.")
                .outroSay(SAM, "Demons aren't supposed to be this active. Something's changing.")
                .reward(ModItems.SALT, 16));

        // 1x05
        list.add(new Chapter(++n, "Bloody Mary", "Toledo, Ohio", Objective.BREAK_CURSED)
                .narrate("A man dies with blood running from his eyes, minutes after his daughter's friends said a name three times into a bathroom mirror.")
                .say(SAM, "It's a sleepover game. Every kid in America has played it.")
                .say(DEAN, "And this time it's real. The spirit lives in the glass and it goes after people with secrets.")
                .narrate("Sam is quiet. He keeps dreaming about Jessica, and the dreams feel like warnings.")
                .goal("Mary's spirit is bound to the mirror she died in front of. Find the cursed mirror (Cursed Object) and break it. Salt and iron keep her off you.")
                .spawnBound(ModEntities.SPIRIT, "Bloody Mary")
                .anchor(ModBlocks.CURSED_OBJECT, "the cursed mirror")
                .outroNarrate("Faced with her own reflection, the spirit tears itself apart and the mirror shatters.")
                .outroNarrate("Mary said Sam had a secret. She was right: he dreamed about Jessica's death days before it happened. He doesn't tell Dean.")
                .reward(ModItems.SILVER_KNIFE, 1));

        // 1x06
        list.add(new Chapter(++n, "Skin", "St. Louis, Missouri", Objective.KILL)
                .narrate("Sam's college friend Rebecca says her brother Zack is charged with murder. Zack swears he wasn't there, but the security camera says otherwise.")
                .say(DEAN, "Two Zacks. One real, one wearing his face.")
                .say(SAM, "Shapeshifter. It sheds its skin when it changes, and only silver kills it.")
                .narrate("In the sewers they find shed skin, teeth... and then the thing starts wearing Dean.")
                .goal("The shapeshifter shrugs off most weapons. Take it down with the Silver Knife.")
                .spawn(ModEntities.SHAPESHIFTER, 1, "Shapeshifter")
                .outroNarrate("The silver goes in, and the creature dies wearing Dean's face.")
                .outroSay(DEAN, "That's going to stick with me. Watching myself go down.")
                .outroNarrate("The police now think Dean Winchester is dead. For the moment, that's one less problem.")
                .reward(ModItems.SALT, 16)
                .reward(Items.BREAD, 8));

        // 1x07
        list.add(new Chapter(++n, "Hook Man", "Eldridge, Iowa", Objective.SALT_BURN)
                .narrate("A college student is killed in a parked car at the local lovers' lane. His date, Lori, only heard scratching on the roof.")
                .say(SAM, "Hook-handed killer. It's the oldest campfire story there is.")
                .say(DEAN, "Except this one's got a name. Jacob Karns, a preacher from the 1860s who killed with a hook. He's buried in the town cemetery.")
                .goal("Salt and burn Jacob Karns' grave. Iron and salt will keep the Hook Man back while you work.")
                .spawnBound(ModEntities.SPIRIT, "The Hook Man")
                .anchor(ModBlocks.GRAVE, "Karns' grave")
                .outroNarrate("The grave burns, but that isn't the end of it: years ago, the silver of the hook was melted down into other pieces.")
                .outroNarrate("The brothers track down every last piece and burn them all. Lori sleeps through the night for the first time in a week.")
                .reward(ModItems.SALT, 16));

        // 1x08
        list.add(new Chapter(++n, "Bugs", "Oasis Plains, Oklahoma", Objective.KILL)
                .narrate("Oasis Plains is a shiny new housing development. One man dies in a pit swarming with beetles. Another is stung to death in his own house.")
                .say(DEAN, "Bugs don't act like this. Not on their own.")
                .say(SAM, "The people who were driven off this land swore no one would ever live on it. The curse comes due tonight.")
                .narrate("Watching a father and son fight across the street, Sam can't help thinking about John.")
                .goal("Survive the cursed swarm. Kill every insect that comes for you.")
                .spawn(EntityType.SILVERFISH, 12, "Cursed Swarm")
                .outroNarrate("At sunrise the swarm pulls back into the ground. The family gets out alive.")
                .outroNarrate("Nobody builds on Oasis Plains again.")
                .outroSay(SAM, "Dad and I never could be in the same room. I don't know if that's ever going to change.")
                .reward(Items.COOKED_BEEF, 8));

        // 1x09
        list.add(new Chapter(++n, "Home", "Lawrence, Kansas", Objective.KILL)
                .narrate("Sam dreams about a woman trapped in a burning house, and he knows the house. It's the one they grew up in. Lawrence, Kansas.")
                .say(DEAN, "I haven't been back there since I was four. I said I'd never go back.")
                .narrate("A new family lives there now, and something in the walls is going after their kids.")
                .say(MISSOURI, "There's two spirits in that house, boys. One of them's angry. The other one... you'll see.")
                .goal("Destroy the poltergeist. Iron weapons hurt it and salt rounds drive it back.")
                .spawn(ModEntities.SPIRIT, 1, "Poltergeist")
                .outroNarrate("The poltergeist is torn out of the house, but not by the brothers.")
                .outroNarrate("Mary Winchester appears in the flames, speaks to her son, and takes the poltergeist with her.")
                .outroNarrate("Missouri has one more secret: John was in town the whole time. He just wasn't ready to face his boys.")
                .reward(ModItems.HOLY_WATER, 2));

        // 1x10
        list.add(new Chapter(++n, "Asylum", "Rockford, Illinois", Objective.SALT_BURN)
                .narrate("The first word from John in months: coordinates to Roosevelt Asylum, Rockford, Illinois.")
                .narrate("Two cops who searched the abandoned asylum came home and turned on their own families.")
                .say(SAM, "Dr. Sanford Ellicott ran the place. His 'treatments' made patients furious. Then he vanished.")
                .say(DEAN, "Find the doctor's bones, end the house calls.")
                .goal("Ellicott's remains are hidden on the grounds. Salt and burn them.")
                .spawnBound(ModEntities.SPIRIT, "Dr. Sanford Ellicott")
                .anchor(ModBlocks.GRAVE, "Ellicott's remains")
                .outroNarrate("The doctor's bones go up in flames, and his patients finally rest.")
                .outroNarrate("Under Ellicott's influence, Sam said things he's been holding in for years. Dean heard every word.")
                .outroSay(DEAN, "Dad gives the orders, we follow them. That's how it's always worked.")
                .outroSay(SAM, "Maybe that's the problem.")
                .reward(ModItems.SALT, 16));

        // 1x11
        list.add(new Chapter(++n, "Scarecrow", "Burkitsville, Indiana", Objective.BREAK_CURSED)
                .narrate("John finally calls and tells them: the thing that killed their mother was a demon, and he's closing in on it.")
                .narrate("He sends Dean to Burkitsville, Indiana, and orders Sam to stay away. Sam goes the other way. Dean goes alone.")
                .narrate("Burkitsville is a perfect orchard town. Every spring, right before the harvest, a couple of travelers disappear.")
                .say(DEAN, "The whole town made a deal with a pagan god. It lives in the scarecrow in the field, and it gets two people a year.")
                .goal("The scarecrow draws its strength from the sacred tree at the heart of the orchard. Weapons won't touch it. Find the tree's heart (Cursed Object) and destroy it.")
                .spawnBound(ModEntities.SCARECROW, "Vanir")
                .anchor(ModBlocks.CURSED_OBJECT, "the sacred tree's heart")
                .outroNarrate("The tree burns. The scarecrow falls apart into straw and rot, and the town's bargain is over.")
                .outroNarrate("Waiting for a bus, Sam meets a friendly girl named Meg. She is not what she seems.")
                .outroNarrate("Sam changes his mind and comes back for his brother.")
                .reward(ModItems.SALT, 16));

        // 1x12
        list.add(new Chapter(++n, "Faith", "Nebraska", Objective.BREAK_CURSED)
                .narrate("Dean is electrocuted on a hunt. His heart is failing, and the doctors give him weeks.")
                .narrate("Sam won't accept that. He finds Roy LeGrange, a faith healer whose prayers actually work. Roy heals Dean.")
                .narrate("But every time Roy heals someone, someone else dies.")
                .say(SAM, "His wife is using black magic. She's bound a reaper, and she's choosing who it takes.")
                .goal("Break the binding spell. Find the altar (Cursed Object) and destroy it before the reaper takes another life.")
                .spawnBound(ModEntities.SPIRIT, "Bound Reaper")
                .anchor(ModBlocks.CURSED_OBJECT, "the binding altar")
                .outroNarrate("The altar breaks and the reaper is free, nobody's weapon anymore.")
                .outroNarrate("Layla, a woman with a brain tumor who waited her turn for a healing, never got one. Dean can't stop thinking about her.")
                .outroSay(DEAN, "She should've been the one who walked out of there. Not me.")
                .reward(Items.GOLDEN_APPLE, 1));

        // 1x13
        list.add(new Chapter(++n, "Route 666", "Cape Girardeau, Missouri", Objective.SALT_BURN)
                .narrate("Dean's ex, Cassie, calls. Black men in her town are being run down by a truck that comes out of nowhere and vanishes just as fast.")
                .narrate("Cassie knows what Dean does. He told her once, and it ended them.")
                .say(SAM, "The driver, Cyrus Deal, died decades ago. He killed people for the color of their skin, and he's still at it.")
                .goal("Cyrus Deal's spirit is tied to where he's buried. Salt and burn his grave.")
                .spawnBound(ModEntities.SPIRIT, "Cyrus Deal")
                .anchor(ModBlocks.GRAVE, "Cyrus Deal's grave")
                .outroNarrate("Dean lures the phantom truck onto the old church ground where Cyrus is buried, and the ghost burns out for good.")
                .outroNarrate("Dean and Cassie say goodbye. Not every story gets a happy ending.")
                .reward(ModItems.SALT, 16));

        // 1x14
        list.add(new Chapter(++n, "Nightmare", "Saginaw, Michigan", Objective.SURVIVE)
                .narrate("Sam has a vision: a man dying in a locked room in Saginaw, Michigan. Then it happens, exactly the way he saw it.")
                .narrate("The cause is Max Miller, a young man who can move things with his mind, and who has a lot of pain behind it.")
                .narrate("Max's mother died when he was a baby, in a fire, the same way Sam's did.")
                .say(SAM, "He's like me, Dean. Whatever happened to our mothers happened to us too.")
                .goal("Max is losing control and everything in the house is flying. Survive the storm while Sam talks him down.")
                .survive(60, 15)
                .spawn(EntityType.VEX, 2, "Flung Blade")
                .outroNarrate("The storm stops. Max is gone, and Sam is shaken. Today he moved a cabinet with his mind.")
                .outroSay(DEAN, "You are not him. You hear me?")
                .outroNarrate("Something chose Sam, and Max, and maybe others. The yellow-eyed demon has plans.")
                .reward(ModItems.HOLY_WATER, 2));

        // 1x15
        list.add(new Chapter(++n, "The Benders", "Hibbing, Minnesota", Objective.KILL)
                .narrate("In Hibbing, Minnesota, people vanish without a trace. A boy says he saw a monster drag a man away.")
                .narrate("Then Sam goes missing too.")
                .say(DEAN, "Ghosts, wendigos, fine. I never figured I'd be this scared of regular people.")
                .narrate("Officer Kathleen Hudak lost her brother here years ago. She's coming with Dean.")
                .goal("The Benders are a family of human hunters. No curse, no magic, just people. Rescue Sam and take the Benders down.")
                .spawn(EntityType.VINDICATOR, 3, "Bender")
                .outroNarrate("Sam is free. The Benders are finished, and Kathleen finally learns what happened to her brother.")
                .outroSay(DEAN, "Give me a monster any day. At least monsters make sense.")
                .reward(ModItems.SALT, 16));

        // 1x16
        list.add(new Chapter(++n, "Shadow", "Chicago, Illinois", Objective.KILL)
                .narrate("Chicago. Victims are torn apart inside locked apartments, each with the same symbol drawn on the floor.")
                .narrate("Sam spots Meg in a bar across town. It's not a coincidence.")
                .say(SAM, "The symbol is Zoroastrian. Daevas: shadow demons. Someone's summoning them and setting them loose.")
                .say(MEG, "You boys are so predictable. Daddy will come running when he hears you're in trouble.")
                .goal("Daevas live in shadow and are nearly untouchable there. Drag the fight into the light (torches, daylight, glowstone) and kill all three.")
                .spawn(ModEntities.DAEVA, 3, "Daeva")
                .outroNarrate("The daevas are banished, and Meg slips away into the night.")
                .outroNarrate("John finally shows up. For one night the family is together, which is exactly what the demons wanted.")
                .outroSay(JOHN, "I have to go. As long as we're together, they can use us against each other.")
                .reward(ModItems.HOLY_WATER, 2)
                .reward(Items.TORCH, 32));

        // 1x17
        list.add(new Chapter(++n, "Hell House", "Richardson, Texas", Objective.BREAK_CURSED)
                .narrate("A ghost-hunting website is spreading a story about Mordechai Murdoch, a farmer said to have murdered his daughters in an old Texas house.")
                .narrate("The story was made up. But thousands of people believe it now, and belief has power.")
                .say(SAM, "It's a tulpa, a thought-form. The more people believe in it, the stronger it gets.")
                .say(DEAN, "And two amateur ghost hunters are about to make it a lot worse.")
                .goal("Mordechai is fed by belief and by a symbol painted in the house. Destroy the symbol (Cursed Object) to break his hold.")
                .spawnBound(ModEntities.SPIRIT, "Mordechai Murdoch")
                .anchor(ModBlocks.CURSED_OBJECT, "the symbol")
                .outroNarrate("With the symbol gone and the website's story rewritten, the tulpa has nothing left to live on. It fades.")
                .outroNarrate("The prank war between the brothers, on the other hand, is far from over.")
                .reward(ModItems.SALT, 16));

        // 1x18
        list.add(new Chapter(++n, "Something Wicked", "Fitchburg, Wisconsin", Objective.KILL)
                .narrate("John sends coordinates: Fitchburg, Wisconsin. Children are falling into comas one after another.")
                .narrate("Dean knows this monster. Years ago, John left him in charge of little Sammy in a motel room. Dean stepped out, and a shtriga came in.")
                .say(DEAN, "I froze. Dad got there just in time. It got away because of me.")
                .say(SAM, "Then we finish it now.")
                .goal("The shtriga can only be hurt by iron. Use iron weapons and salt rounds; nothing else touches it.")
                .spawn(ModEntities.SHTRIGA, 1, "Shtriga")
                .outroNarrate("This time Dean doesn't freeze. The shtriga falls, and in the hospital, the children open their eyes.")
                .outroSay(DEAN, "Twenty years late. But it's done.")
                .reward(ModItems.SALT, 16));

        // 1x19
        list.add(new Chapter(++n, "Provenance", "New Paltz, New York", Objective.SALT_BURN)
                .narrate("An old painting of the Merchant family keeps changing hands, and wherever it hangs, a whole family dies.")
                .narrate("The auction house owner's daughter, Sarah Blake, is smart, funny and very interested in Sam. Dean is thrilled.")
                .say(SAM, "The story says the father, Isaac Merchant, killed his whole family. I'm not so sure.")
                .goal("The spirit in the painting is tied to its remains. Salt and burn them.")
                .spawnBound(ModEntities.SPIRIT, "Spirit in the Painting")
                .anchor(ModBlocks.GRAVE, "the hidden remains")
                .outroNarrate("The first grave they burn is the wrong one. The real killer was the little girl in the painting, and her remains were hidden closer than anyone guessed.")
                .outroNarrate("The painting burns. Sam and Sarah say goodbye on good terms, and for once Sam looks like he might believe in a life after all this.")
                .reward(ModItems.SALT, 16));

        // 1x20
        list.add(new Chapter(++n, "Dead Man's Blood", "Manning, Colorado", Objective.KILL)
                .narrate("Daniel Elkins, an old hunter John worked with, is murdered in Manning, Colorado. His killers took something from his safe.")
                .narrate("John is there too, looking for the same thing: the Colt, a revolver made in 1835 that can kill anything.")
                .say(JOHN, "Vampires. I thought they were all gone. Elkins knew better, and they killed him for it.")
                .say(DEAN, "So, we take their heads. And dead man's blood knocks them out.")
                .goal("Vampires heal from almost anything. Poison them with Dead Man's Blood (right-click one), then take their heads with the Machete or an axe. Kill all four.")
                .startWith(ModItems.MACHETE, 1)
                .startWith(ModItems.DEAD_MANS_BLOOD, 4)
                .spawn(ModEntities.VAMPIRE, 4, "Vampire")
                .outroNarrate("The nest is wiped out and the Colt is back in the Winchesters' hands.")
                .outroSay(JOHN, "There's a gun that can kill the thing that took your mother. And now we have it.")
                .outroNarrate("For the first time in a long time, John looks at his sons like partners, not soldiers.")
                .reward(ModItems.THE_COLT, 1)
                .reward(ModItems.COLT_ROUND, 6));

        // 1x21
        list.add(new Chapter(++n, "Salvation", "Salvation, Iowa", Objective.EXORCISE)
                .narrate("John has worked out the demon's pattern. It comes for children on their six-month birthday, and in Salvation, Iowa, a baby girl named Rosie is about to turn six months old.")
                .narrate("Meg calls with a threat: hand over the Colt or she keeps killing John's friends. Pastor Jim is already dead.")
                .say(JOHN, "You boys protect that family. I'll deal with Meg.")
                .goal("Two demons stand between you and Rosie's nursery. Trap and exorcise them, or burn them out with Holy Water.")
                .startWith(ModItems.HOLY_WATER, 4)
                .spawn(ModEntities.DEMON, 2, "Meg's Henchman")
                .outroNarrate("Sam reaches the nursery in time. The demon is there: yellow eyes, the room catching fire again. Sam has one shot.")
                .outroNarrate("It's gone before he can pull the trigger. Rosie and her family are safe. The demon is not dead.")
                .outroNarrate("John walks into Meg's trap carrying a fake Colt, and he doesn't come back.")
                .reward(ModItems.HOLY_WATER, 2));

        // 1x22 part 1
        list.add(new Chapter(++n, "Devil's Trap (Part 1)", "Sioux Falls, South Dakota", Objective.EXORCISE)
                .narrate("With John captured, the brothers go to Bobby Singer: an old friend, a hunter, and the closest thing they have to family outside the car.")
                .say(BOBBY, "You two are in way over your heads. Lucky for you I've got a library in my basement and a trap painted on my ceiling.")
                .narrate("Meg comes to them. Of course she does.")
                .say(MEG, "Give me the Colt, and maybe Daddy comes home in one piece.")
                .goal("Meg is a demon riding an innocent woman. Lure her into a Devil's Trap and exorcise her.")
                .startWith(ModItems.DEVILS_TRAP, 4)
                .startWith(ModItems.EXORCISM_RITE, 1)
                .spawn(ModEntities.DEMON, 1, "Meg Masters")
                .outroNarrate("Held in the trap and burning under the rite, the demon spits out where John is being held: Jefferson City, Missouri.")
                .outroNarrate("The demon is cast out. The woman it was riding has just enough time to point the way before her injuries take her.")
                .reward(ModItems.COLT_ROUND, 2));

        // 1x22 part 2
        list.add(new Chapter(++n, "Devil's Trap (Part 2)", "Jefferson City, Missouri", Objective.BOSS)
                .narrate("Jefferson City. An apartment building crawling with demons. John is upstairs.")
                .narrate("Dean has the real Colt. Sam has a plan. Neither of them likes the plan.")
                .narrate("They get John out. He's quiet. Too quiet. And he keeps asking for the gun.")
                .say(DEAN, "Dad would be furious I'm still holding his gun. You're not even blinking. Who are you?")
                .narrate("The yellow eyes open.")
                .goal("The Yellow-Eyed Demon is riding John. Nothing you carry can push it below half health. You need the Colt. Drive it out!")
                .startWith(ModItems.COLT_ROUND, 2)
                .spawn(ModEntities.GREATER_DEMON, 1, "The Yellow-Eyed Demon")
                .outroNarrate("The Colt fires. The demon howls and pours out of John in a column of black smoke. It's wounded, not dead, and it got away.")
                .outroNarrate("Bleeding and exhausted, all three Winchesters pile into the Impala and head for a hospital.")
                .outroNarrate("They never see the truck coming.")
                .outroNarrate("A big rig with a black-eyed driver slams into the Impala. Three bodies, not moving. The radio keeps playing.")
                .reward(Items.DIAMOND, 3));

        return list;
    }

    private Season1() {
    }
}
