package eu.ecodex.dc5.message.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.style.ToStringCreator;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


/**
 * @author riederb
 * @version 1.0
 */
@Embeddable

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DC5Action {

	@NotNull
	@NotBlank
	@Column(nullable=false)
	private String action;

	public String getAction(){
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
    public String toString() {
        ToStringCreator builder = new ToStringCreator(this);
        builder.append("action", this.action);
        return builder.toString();        
    }
    
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DC5Action other = (DC5Action) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		return true;
	}
}