package dhbw.smartmoderation.group.personInfo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.data.model.Group;
import dhbw.smartmoderation.data.model.Member;
import dhbw.smartmoderation.data.model.Role;
import dhbw.smartmoderation.exceptions.GroupNotFoundException;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

public class PersonInfoActivity extends ExceptionHandlingActivity {

    private TextView name;
    private TextView moderatorText;
    private RadioGroup moderatorSwitch;
    private TextView guestText;
    private RadioGroup guestSwitch;
    private Button linkContactButton;
    private Long groupId;
    private Long memberId;
    private PersonInfoController controller;
    private Group group;
    private Member member;
    private RadioGroup.OnCheckedChangeListener moderatorListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int id = moderatorSwitch.getCheckedRadioButtonId();

            if(id == R.id.moderatorOn) {

                PersonInfoAsyncTask personInfoAsyncTask = new PersonInfoAsyncTask("addRole");
                personInfoAsyncTask.execute(member, Role.MODERATOR);
            }

            else {

                if(controller.isLocalAuthor(memberId))  {

                    if(controller.countModeratorsInGroup() > 1) {

                        PersonInfoAsyncTask personInfoAsyncTask = new PersonInfoAsyncTask("removeRole");
                        personInfoAsyncTask.execute(member, Role.MODERATOR);
                    }

                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PersonInfoActivity.this);
                        builder.setMessage(getString(R.string.canNotRemoveModeratorRights));
                        builder.setCancelable(false);
                        builder.setNeutralButton(getString(R.string.ok), (dialog, which) -> {
                            dialog.cancel();
                        });

                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                        moderatorSwitch.setOnCheckedChangeListener(null);
                        moderatorSwitch.check(R.id.moderatorOn);
                        moderatorSwitch.setOnCheckedChangeListener(moderatorListener);
                    }
                }

                else {

                    PersonInfoAsyncTask personInfoAsyncTask = new PersonInfoAsyncTask("removeRole");
                    personInfoAsyncTask.execute(member, Role.MODERATOR);
                }
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener guestListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            int id = guestSwitch.getCheckedRadioButtonId();

            if(id == R.id.guestOn) {

                if(controller.isPollOpen()) {

                  showAlertDialogForGuestSwitch(getString(R.string.canNotBeMarkedAsSpectator));
                  return;

                }

                if(controller.countParticipantsInGroup() < 2) {

                    showAlertDialogForGuestSwitch(getString(R.string.lastParticipant));
                    return;
                }

                PersonInfoAsyncTask personInfoAsyncTask = new PersonInfoAsyncTask("changeRole");
                personInfoAsyncTask.execute(member, Role.PARTICIPANT, Role.SPECTATOR);
            }

            else {

                PersonInfoAsyncTask personInfoAsyncTask = new PersonInfoAsyncTask("changeRole");
                personInfoAsyncTask.execute(member, Role.SPECTATOR, Role.PARTICIPANT);
            }
        }
    };

    public void showAlertDialogForGuestSwitch(String text) {

        AlertDialog.Builder builder = new AlertDialog.Builder(PersonInfoActivity.this);
        builder.setMessage(text);
        builder.setCancelable(false);
        builder.setNeutralButton(getString(R.string.ok), (dialog, which) -> {
            dialog.cancel();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        guestSwitch.setOnCheckedChangeListener(null);
        guestSwitch.check(R.id.guestOff);
        guestSwitch.setOnCheckedChangeListener(guestListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_info);
        setTitle(getString(R.string.PersonInfo_title));

        name = findViewById(R.id.personInfoName);

        moderatorText = findViewById(R.id.moderatorTag);
        moderatorSwitch = findViewById(R.id.moderatorSwitch);
        guestText = findViewById(R.id.guestTag);
        guestSwitch = findViewById(R.id.guestSwitch);

        linkContactButton = findViewById(R.id.linkContactButton);
        linkContactButton.setVisibility(View.GONE);

        Intent intent = getIntent();
        memberId = intent.getLongExtra("memberId", 0);
        groupId = intent.getLongExtra("groupId", 0);

        controller = new PersonInfoController(groupId);

        group = controller.getGroup();

        member = group.getMember(memberId);

        name.setText(member.getName());

        linkContactButton.setOnClickListener(v -> {

            Intent i = new Intent(this, AddContactActivity.class);
            i.putExtra("memberId", this.memberId);
            i.putExtra("groupId", this.groupId);

            startActivityForResult(i, 0);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.memberId = data.getLongExtra("memberId", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        update();

    }

    @Override
    protected void onPause() {
        super.onPause();

        PersonInfoAsyncTask personInfoAsyncTask = new PersonInfoAsyncTask("submit");
        personInfoAsyncTask.execute();
    }

    public void update() {

        moderatorSwitch.setOnCheckedChangeListener(null);
        guestSwitch.setOnCheckedChangeListener(null);

        if(member.getRoles(group).contains(Role.MODERATOR)) {
            moderatorSwitch.check(R.id.moderatorOn);
        }

        else {
            moderatorSwitch.check(R.id.moderatorOff);
        }

        if(member.getRoles(group).contains(Role.SPECTATOR)) {
            guestSwitch.check(R.id.guestOn);
        }

        else {
            guestSwitch.check(R.id.guestOff);
        }

        if(member.getIsGhost()) {
            moderatorText.setVisibility(View.GONE);
            moderatorSwitch.setVisibility(View.GONE);
            guestText.setVisibility(View.GONE);
            guestSwitch.setVisibility(View.GONE);

            if(controller.isLocalAuthorModerator()) {

                linkContactButton.setVisibility(View.VISIBLE);
            }
        }

        else if (!controller.isLocalAuthorModerator()) {

            for (int i = 0; i < moderatorSwitch.getChildCount(); i++) {

                (moderatorSwitch.getChildAt(i)).setEnabled(false);
            }

            for (int i = 0; i < guestSwitch.getChildCount(); i++) {

                (guestSwitch.getChildAt(i)).setEnabled(false);
            }
        }

        moderatorSwitch.setOnCheckedChangeListener(moderatorListener);
        guestSwitch.setOnCheckedChangeListener(guestListener);
    }


    public class PersonInfoAsyncTask extends AsyncTask<Object, Exception, String> {

        String flag;

        public PersonInfoAsyncTask(String flag) {

            this.flag = flag;

        }

        @Override
        protected void onProgressUpdate(Exception... values) {
            super.onProgressUpdate(values);
            handleException(values[0]);
        }

        @Override
        protected String doInBackground(Object... objects) {

            switch(flag) {

                case "addRole":
                    Member memberToAdd = (Member)objects[0];
                    Role roleToAdd = (Role)objects[1];
                    controller.addRole(memberToAdd, roleToAdd);
                    break;

                case "removeRole":
                    Member memberToRemove = (Member)objects[0];
                    Role roleToRemove = (Role)objects[1];
                    controller.removeRole(memberToRemove, roleToRemove);
                    break;

                case "changeRole":
                    Member member = (Member)objects[0];
                    Role previousRole = (Role)objects[1];
                    controller.removeRole(member, previousRole);
                    Role role = (Role)objects[2];
                    controller.addRole(member, role);
                    break;

                case "submit":
                    try {

                        controller.submitChanges();

                    } catch (GroupNotFoundException exception) {

                        publishProgress(exception);
                    }
            }

            return flag;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            switch (flag) {

                case "submit":
                    finish();
            }
        }
    }
}