package dhbw.smartmoderation.exceptions;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collection;
import java.util.List;

import dhbw.smartmoderation.group.detail.MemberAdapter;

//TODO Obsolete Klasse löschen
public class ExceptionAdapter extends RecyclerView.Adapter<ExceptionAdapter.ExceptionViewHolder> {



    private List<Throwable> Exceptions;
    private Context Context;

    public ExceptionAdapter(Context context, List<Throwable> exceptions){
        Context = context;
        Exceptions = exceptions;
    }

    @NonNull
    @Override
    public ExceptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout layout = new ConstraintLayout(Context);
        layout
                .setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        ExceptionAdapter.ExceptionViewHolder memberViewHolder = new ExceptionAdapter.ExceptionViewHolder(layout, Context);
        return memberViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ExceptionViewHolder holder, int position) {
        Throwable exception = Exceptions.get(position);

        holder.setException(exception);
    }

    @Override
    public int getItemCount() {
        return Exceptions.size();
    }


    static class ExceptionViewHolder extends RecyclerView.ViewHolder{

        private Context Context;
        private TextView Message;
        private ListView Stacktrace;

        public ExceptionViewHolder(@NonNull ConstraintLayout itemView, Context context) {
            super(itemView);

            Context = context;



        }

        public void setException(Throwable exception){
        }
    }
}
