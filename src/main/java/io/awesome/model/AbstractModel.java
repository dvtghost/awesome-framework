package io.awesome.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class AbstractModel implements BaseModel<String>, Serializable {
  @Id
  @GeneratedValue(generator = "system-uuid")
  @GenericGenerator(name = "system-uuid", strategy = "uuid")
  private String id;

  @Getter @Setter @Version protected Integer version;
  @Getter @Setter protected String createdBy;
  @Getter @Setter protected String updatedBy;

  @Getter
  @Setter
  @Column(updatable = false)
  @CreationTimestamp
  protected LocalDateTime createdOn;

  @Getter @Setter @UpdateTimestamp protected LocalDateTime updatedOn;

  // this is used for preSave & postSave method communications!
  @Transient @Getter @Setter private Boolean creatingNewObject = false;

  public AbstractModel(String id) {
    this.id = id;
  }

  public AbstractModel() {}

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    if (id != null && id.length() > 0) this.id = id;
    else this.id = null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    AbstractModel other = (AbstractModel) obj;
    if (id == null) {
      return other.id == null;
    } else return id.equals(other.id);
  }

  @Override
  public abstract String toString();
}
