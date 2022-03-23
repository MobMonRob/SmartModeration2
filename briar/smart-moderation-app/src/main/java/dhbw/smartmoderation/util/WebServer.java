package dhbw.smartmoderation.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import dhbw.smartmoderation.SmartModerationApplication;
import dhbw.smartmoderation.data.model.ConsensusLevel;
import dhbw.smartmoderation.data.model.Meeting;
import dhbw.smartmoderation.data.model.ModerationCard;
import dhbw.smartmoderation.data.model.Poll;
import dhbw.smartmoderation.data.model.Voice;
import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    public static final int PORT = 8765;
    public SmartModerationApplication app;
    public Long meetingId;

    public WebServer(SmartModerationApplication app) {
        super(PORT);
        this.app = app;
    }

    @Override
    public Response serve(IHTTPSession session) {

        String uri = session.getUri();
        Method method = session.getMethod();
        String filename = uri.substring(1);
        String mimetype = "text/html";
        boolean is_ascii = true;

       if(uri.equals("/polls")) {

           mimetype ="application/json";

           try {

               JSONArray pollArray  = new JSONArray();

               for(Poll poll : getMeeting().getPolls()) {

                   JSONObject pollJSON = new JSONObject();

                   pollJSON.put("id", String.valueOf(poll.getPollId()))
                           .put("title", poll.getTitle())
                           .put("consensusProposal", poll.getConsensusProposal());

                   pollArray.put(pollJSON);
               }

               JSONObject meetingJSON = new JSONObject();

               meetingJSON.put("meetingId", String.valueOf(getMeeting().getMeetingId()))
                       .put("name", getMeeting().getCause())
                       .put("polls", pollArray);

               return newFixedLengthResponse(Response.Status.OK, mimetype, meetingJSON.toString());

           } catch (JSONException e) {

               e.printStackTrace();
           }
        }

       else if (uri.equals("/moderationcards")){

           mimetype ="application/json";

           try {

               JSONArray cardsArray  = new JSONArray();

               for(ModerationCard card : getMeeting().getModerationCards()) {

                   JSONObject cardJSON = new JSONObject();
                   String  hexColor = String.format("#%06X", (0xFFFFFF & card.getColor()));
                   cardJSON.put("cardId", card.getCardId())
                           .put("content", card.getContent())
                           .put("color", hexColor)
                           .put("meetingId", card.getMeetingId());

                   cardsArray.put(cardJSON);
               }

               return newFixedLengthResponse(Response.Status.OK, mimetype, cardsArray.toString());

           } catch (JSONException e) {

               e.printStackTrace();
           }
       }

       else if (uri.contains("/result/voices") &&  Method.GET.equals(method)) {

           mimetype = "application/json";

           Long pollId = Long.parseLong(session.getParameters().get("pollId").get(0));

           try {

               JSONArray voiceArray = new JSONArray();

               for(Voice voice : getMeeting().getPoll(pollId).getVoices()) {

                   JSONObject voiceJSON = new JSONObject();

                   voiceJSON.put("consensusLevel", String.valueOf(voice.getConsensusLevel().getConsensusLevelId()))
                            .put("explanation", voice.getExplanation())
                            .put("member", voice.getMember().getName());

                   voiceArray.put(voiceJSON);
               }

               JSONObject pollJSON = new JSONObject();
               pollJSON.put("pollId", String.valueOf(pollId))
                       .put("name", getMeeting().getPoll(pollId).getTitle())
                       .put("voices", voiceArray);

               return newFixedLengthResponse(Response.Status.OK, mimetype, pollJSON.toString());

           } catch (JSONException e) {

               e.printStackTrace();
           }

       }

       else if(uri.contains("/result/consensusLevels") &&  Method.GET.equals(method)) {

           mimetype = "application/json";

           try {

               JSONArray consensusLevelArray = new JSONArray();

               for(ConsensusLevel consensusLevel : getMeeting().getGroup().getGroupSettings().getConsensusLevels()) {

                   JSONObject consensusLevelJSON = new JSONObject();

                   String hexColor = String.format("#%06X", (0xFFFFFF & consensusLevel.getColor()));

                   consensusLevelJSON.put("id", String.valueOf(consensusLevel.getConsensusLevelId()))
                                     .put("color",  hexColor)
                                     .put("name", consensusLevel.getName());

                   consensusLevelArray.put(consensusLevelJSON);
               }

               JSONObject consensusLevelJSON = new JSONObject();
               consensusLevelJSON.put("consensusLevels", consensusLevelArray);

               return newFixedLengthResponse(Response.Status.OK, mimetype, consensusLevelJSON.toString());

           } catch (JSONException e) {

               e.printStackTrace();
           }

       }

       else if (uri.contains("/result/members") && Method.GET.equals(method)) {

           mimetype = "application/json";

           int count = getMeeting().getPresentVoteMembers().size();

           JSONObject countJSON = new JSONObject();

           try {

               countJSON.put("count", count);

               return newFixedLengthResponse(Response.Status.OK, mimetype, countJSON.toString());

           } catch (JSONException e) {

               e.printStackTrace();
           }
       }

        else if(uri.equals("/")) {
            filename ="overview.html";
            mimetype = "text/html";
            is_ascii = true;
        }

        else if(uri.equals("/result")) {
            filename ="result.html";
            mimetype = "text/html";
            is_ascii = true;
        }

        else {

            if(uri.contains("/result")) {

                filename = uri.substring(1);
            }

            if(filename.contains(".html")) {
                mimetype = "text/html";
                is_ascii = true;
            }

            else if (filename.contains(".css")) {
                mimetype = "text/css";
                is_ascii = true;
            }

            else if (filename.contains(".js")) {
                mimetype = "application/javascript";
                is_ascii = true;
            }

            else if(filename.contains(".gif")) {
                mimetype = "text/gif";
                is_ascii = false;
            }

            else if(filename.contains(".jpeg") || filename.contains(".jpg")) {
                mimetype = "text/jpeg";
                is_ascii = false;
            }

            else if(filename.contains(".png")) {
                mimetype = "image/png";
                is_ascii = false;
            }
        }

        if(is_ascii) {

            StringBuilder response = new StringBuilder();
            String line = "";

            try {

                BufferedReader reader = new BufferedReader(new InputStreamReader(app.getApplicationContext().getAssets().open(filename)));

                while((line = reader.readLine()) != null) {

                    response.append(line);
                }

                reader.close();

            } catch (IOException e) {

                e.printStackTrace();
            }

            return newFixedLengthResponse(Response.Status.OK, mimetype, response.toString());

        }

        else {

            try {

                InputStream inputStream = app.getApplicationContext().getAssets().open(filename);
                return newFixedLengthResponse(Response.Status.OK, mimetype, inputStream, inputStream.available());

            } catch(IOException e) {

                e.printStackTrace();
            }

        }

        return super.serve(session);
    }

    public void setMeetingId(Long meetingId) {

        this.meetingId = meetingId;
    }

    public Long getMeetingId() {
        return this.meetingId;
    }

    public Meeting getMeeting() {

        for(Meeting meeting : app.getDataService().getMeetings()) {

            if(meeting.getMeetingId().equals(meetingId)) {

                return meeting;
            }
        }


        return null;
    }
}
