package dhbw.smartmoderation.util.Mocks;

import java.util.Collection;

import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.exceptions.CouldNotAddConsensusLevel;
import dhbw.smartmoderation.exceptions.CouldNotChangeConsensusLevel;
import dhbw.smartmoderation.exceptions.GroupSettingsNotFoundException;

public interface ISettings {

    Collection<ConsensusLevel> getConsensusLevels() throws GroupSettingsNotFoundException;

    void addConsensusLevel(ConsensusLevel consensusLevel) throws GroupSettingsNotFoundException, CouldNotAddConsensusLevel;

    void changeConsensusLevel(ConsensusLevel consensusLevel) throws CouldNotChangeConsensusLevel;

    void changeConsensusLevelNumbering(Collection<ConsensusLevel> consensusLevels) throws CouldNotChangeConsensusLevel;

    ConsensusLevel createConsensusLevel(String name, int color, String description, int number);
}
