package app.msg;

public interface TextMessage extends Message {

	String getText();

	void setText(String value);
}
