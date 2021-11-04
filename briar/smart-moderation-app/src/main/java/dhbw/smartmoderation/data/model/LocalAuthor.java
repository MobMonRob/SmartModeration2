package dhbw.smartmoderation.data.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class LocalAuthor {

    @Id
    private Long localAuthorId;

    @Generated(hash = 2036940863)
    public LocalAuthor(Long localAuthorId) {
        this.localAuthorId = localAuthorId;
    }

    @Generated(hash = 925913843)
    public LocalAuthor() {
    }

    public Long getLocalAuthorId() {
        return this.localAuthorId;
    }

    public void setLocalAuthorId(Long localAuthorId) {
        this.localAuthorId = localAuthorId;
    }

}
