package dhbw.smartmoderation.group.personInfo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.briarproject.bramble.api.contact.Contact;

import dhbw.smartmoderation.R;
import dhbw.smartmoderation.exceptions.CantLinkContactToMember;
import dhbw.smartmoderation.exceptions.SmartModerationException;
import dhbw.smartmoderation.util.ExceptionHandlingActivity;

public class AddContactActivity extends ExceptionHandlingActivity {

    private ContactAdapter contactAdapter;
    private PersonInfoController controller;
    private TextView ghostName;
    private RecyclerView addContactList;
    private FloatingActionButton doneFab;
    private LinearLayoutManager contactLayoutManager;
    private Long memberId;
    private Long groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        setTitle(getString(R.string.AddContact_PersonInfo_title));

        Intent intent = getIntent();
        memberId = intent.getLongExtra("memberId", 0);
        groupId = intent.getLongExtra("groupId", 0);

        ghostName = findViewById(R.id.ghostName);
        addContactList = findViewById(R.id.addContactList);
        doneFab = findViewById(R.id.doneFab);
        doneFab.setOnClickListener(v -> {

            AddContactAsyncTask addContactAsyncTask = new AddContactAsyncTask();
            addContactAsyncTask.execute();

        });

        controller = new PersonInfoController(this.groupId);
        ghostName.setText(controller.getGroup().getMember(this.memberId).getName());
        contactLayoutManager = new LinearLayoutManager(this);
        addContactList.setLayoutManager(contactLayoutManager);

        try {

            contactAdapter = new ContactAdapter(this, controller.getContacts());
            addContactList.setAdapter(contactAdapter);

        } catch (SmartModerationException exception){

            handleException(exception);
        }

        DividerItemDecoration contactsDividerItemDecoration = new DividerItemDecoration(addContactList.getContext(), contactLayoutManager.getOrientation());
        addContactList.addItemDecoration(contactsDividerItemDecoration);
    }

    public class AddContactAsyncTask extends AsyncTask<String, Exception, Long> {

        @Override
        protected void onProgressUpdate(Exception... values) {
            super.onProgressUpdate(values);
            handleException(values[0]);
        }

        @Override
        protected Long doInBackground(String... strings) {

            try {

                Contact selectedContact = contactAdapter.getSelectedContact();
                Long memberId = controller.linkContactToMember(selectedContact, AddContactActivity.this.memberId);
                return memberId;

            } catch (CantLinkContactToMember exception) {

                publishProgress(exception);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Long l) {
            super.onPostExecute(l);

            if(l != null) {

                Intent i = new Intent();
                i.putExtra("memberId", l);
                setResult(Activity.RESULT_OK, i);
                finish();
            }
        }
    }
}