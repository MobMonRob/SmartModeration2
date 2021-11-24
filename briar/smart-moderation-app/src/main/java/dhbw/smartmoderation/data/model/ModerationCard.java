package dhbw.smartmoderation.data.model;

import android.graphics.Color;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ModerationCard {

    @Id
    private Long cardId;
    private String content;
    @Generated(hash = 354965275)
    public ModerationCard(Long cardId, String content) {
        this.cardId = cardId;
        this.content = content;
    }
    @Generated(hash = 2036705586)
    public ModerationCard() {
    }
    public Long getCardId() {
        return this.cardId;
    }
    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
}
