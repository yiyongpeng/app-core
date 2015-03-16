package app.msg;

import java.io.Serializable;

public interface ObjectMessage extends Message {

	Serializable getObject();

	void setObject(Serializable value);
}
