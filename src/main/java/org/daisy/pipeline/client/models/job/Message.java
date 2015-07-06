package org.daisy.pipeline.client.models.job;

/**
 * A job message.
 * 
 * @author jostein
 */
public class Message implements Comparable<Message> {
	
	public enum Level { ERROR, WARNING, INFO, DEBUG, TRACE };
	
	public Level level;
	public Integer sequence;
	public String text;
	
	public Message(Level level, int sequence, String text) {
		this.level = level;
		this.sequence = sequence;
		this.text = text;
	}
	
	public Message(String level, String sequence, String text) {
		for (Level l : Level.values()) {
			if (l.toString().equals(level)) {
				this.level = l;
				break;
			}
		}
		
		this.sequence = Integer.parseInt(sequence);
		
		this.text = text;
	}

//	@Override
	public int compareTo(Message other) {
		if (!(other instanceof Message))
			return 0;
		
		return this.sequence.compareTo(((Message)other).sequence);
	}
	
}