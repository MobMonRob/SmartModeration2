package dhbw.smartmoderation.moderationCard.detail;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.briarproject.briar.api.privategroup.PrivateGroup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import dhbw.smartmoderation.connection.synchronization.SynchronizationService;
import dhbw.smartmoderation.data.DataService;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.exceptions.CantEditModerationCardException;
import dhbw.smartmoderation.exceptions.CouldNotDeleteModerationCard;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.moderationCard.ModerationCardServiceController;
import dhbw.smartmoderation.moderationCard.create.CreateModerationCardController;

public class DetailModerationCardControllerTest {

    private DetailModerationCardController detailModerationCardController;
    private ModerationCard moderationCard;

    @Before
    public void setUp() throws GroupNotFoundException, MeetingNotFoundException, ModerationCardNotFoundException {
        moderationCard = new ModerationCard(12345678L, "content", "author", 42, 4711, 87654321L);
        ModerationCardServiceController moderationCardServiceControllerMock = Mockito.mock(ModerationCardServiceController.class);
        DataService dataServiceMock = mock(DataService.class);
        SynchronizationService synchronizationServiceMock = mock(SynchronizationService.class);
        PrivateGroup privateGroupMock = mock(PrivateGroup.class);
        when(moderationCardServiceControllerMock.getPrivateGroup(any())).thenReturn(privateGroupMock);
        when(moderationCardServiceControllerMock.getSynchronizationService()).thenReturn(synchronizationServiceMock);
        doNothing().when(synchronizationServiceMock).push(any(), any());
        doNothing().when(dataServiceMock).deleteModerationCard(any());
        when(moderationCardServiceControllerMock.getDataService()).thenReturn(dataServiceMock);
        Meeting meetingMock = mock(Meeting.class);
        when(dataServiceMock.getMeeting(anyLong())).thenReturn(meetingMock);
        doNothing().when(dataServiceMock).mergeModerationCard(any());

        when(dataServiceMock.getModerationCard(anyLong())).thenReturn(moderationCard);
        doNothing().when(dataServiceMock).deleteModerationCard(anyLong());

        detailModerationCardController = new DetailModerationCardController(12345678,moderationCardServiceControllerMock);

    }

    @Test
    public void editModerationCard() throws ModerationCardNotFoundException, CantEditModerationCardException, MeetingNotFoundException {

        ModerationCard generatedModerationCard = detailModerationCardController.editModerationCard("content", "author", 42, 4711, 12345678L);
        assertNotNull(generatedModerationCard);
        assertEquals(generatedModerationCard.getAuthor(), moderationCard.getAuthor());
        assertEquals(generatedModerationCard.getBackgroundColor(),moderationCard.getBackgroundColor());
        assertEquals(generatedModerationCard.getContent(), moderationCard.getContent());
        assertEquals(generatedModerationCard.getFontColor(), moderationCard.getFontColor());
        assertEquals(generatedModerationCard.getCardId(), moderationCard.getCardId());
    }

    @Test
    public void deleteModerationCard() throws CouldNotDeleteModerationCard, ModerationCardNotFoundException, MeetingNotFoundException {

        detailModerationCardController.deleteModerationCard(12345678L);
        assertTrue(moderationCard.isDeleted());

    }
}