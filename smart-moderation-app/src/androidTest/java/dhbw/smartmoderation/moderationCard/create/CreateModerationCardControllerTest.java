package dhbw.smartmoderation.moderationCard.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import org.briarproject.briar.api.privategroup.PrivateGroup;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import dhbw.smartmoderation.connection.synchronization.SynchronizationService;
import dhbw.smartmoderation.data.DataService;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.exceptions.CantCreateModerationCardException;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.exceptions.MeetingNotFoundException;
import dhbw.smartmoderation.exceptions.ModerationCardNotFoundException;
import dhbw.smartmoderation.moderationCard.ModerationCardServiceController;

public class CreateModerationCardControllerTest {

    private CreateModerationCardController createModerationCardController;

    @Before
    public void setUp() throws GroupNotFoundException, MeetingNotFoundException {

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

        createModerationCardController = new CreateModerationCardController(12345678,moderationCardServiceControllerMock);

    }

    @Test
    public void createModerationCard() throws CantCreateModerationCardException, ModerationCardNotFoundException, MeetingNotFoundException {

        ModerationCard moderationCard = new ModerationCard(12345678L, "content", "author", 42, 4711, 87654321L);
        ModerationCard generatedModerationCard = createModerationCardController.createModerationCard("content", "author", 42, 4711);
        assertNotNull(generatedModerationCard);
        assertEquals(generatedModerationCard.getAuthor(), moderationCard.getAuthor());
        assertEquals(generatedModerationCard.getBackgroundColor(),moderationCard.getBackgroundColor());
        assertEquals(generatedModerationCard.getContent(), moderationCard.getContent());
        assertEquals(generatedModerationCard.getFontColor(), moderationCard.getFontColor());
    }
}