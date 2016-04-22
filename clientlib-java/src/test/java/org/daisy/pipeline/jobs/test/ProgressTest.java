package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Job.Status;
import org.daisy.pipeline.client.models.Message;
import org.junit.Test;

public class ProgressTest {
	
	private int sequence = 0;
	private void addMessage(Job job, Long time, String text) {
		Message m = new Message();
		m.timeStamp = time;
		m.text = text;
		m.level = Message.Level.DEBUG;
		m.sequence = sequence++;
		
		List<Message> messages = job.getMessages();
		if (messages == null) {
			messages = new ArrayList<Message>();
		}
		messages.add(m);
		job.setMessages(messages);
	}
	
	final double delta = 0.01;

	@Test
	public void testProgressForStatesAndNoMessages() {
		
		Job job = new Job();
		
		job.setStatus(null);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(), delta);
		
		job.setStatus(Status.IDLE);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(), delta);
		
		job.setStatus(Status.RUNNING);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		
		job.setStatus(Status.DONE);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(100.0, job.getProgressEstimate(0L), delta);
		
		job.setStatus(Status.ERROR);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(100.0, job.getProgressEstimate(0L), delta);
		
		job.setStatus(Status.VALIDATION_FAIL);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(100.0, job.getProgressEstimate(0L), delta);
	}

	@Test
	public void testProgressForASimulatedJob() {
		Job job = new Job();
		
		// job queued
		job.setStatus(Status.IDLE);
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(), delta);
		
		job.setStatus(Status.RUNNING);
		addMessage(job, 0L, "[progress 0] ...");
		assertEquals("[progress 0] ...", job.getMessages().get(job.getMessages().size()-1).text);
		assertEquals("...", job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(0.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(0.0, job.getProgressEstimate(0L), delta);
		assertEquals(95.02, job.getProgressEstimate(60000L), delta);
		
		addMessage(job, 5000L, "[progress 5] ...");
		assertEquals("[progress 5] ...", job.getMessages().get(job.getMessages().size()-1).text);
		assertEquals("...", job.getMessages().get(job.getMessages().size()-1).getText());
		assertEquals(5.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(5.0, job.getProgressEstimate(5000L), delta);
		
		addMessage(job, 10000L, "[progress 10-20] ...");
		assertEquals(10.0, job.getProgressFrom(), delta);
		assertEquals(20.0, job.getProgressTo(), delta);
		assertEquals(10.0, job.getProgressEstimate(10000L), delta);
		assertEquals(19.5, job.getProgressEstimate(20000L), delta);
		
		addMessage(job, 20000L, "[progress 20-70] ...");
		assertEquals(20.0, job.getProgressFrom(), delta);
		assertEquals(70.0, job.getProgressTo(), delta);
		assertEquals(20.0, job.getProgressEstimate(20000L), delta);
		
		addMessage(job, 70000L, "[progress 70-100] ...");
		assertEquals(70.0, job.getProgressFrom(), delta);
		assertEquals(100.0, job.getProgressTo(), delta);
		assertEquals(70.0, job.getProgressEstimate(70000L), delta);
		assertEquals(98.5, job.getProgressEstimate(100000L), delta);
		
		job.setStatus(Status.DONE);
		assertEquals(100.0, job.getProgressEstimate(), delta);
		job.setStatus(Status.ERROR);
		assertEquals(100.0, job.getProgressEstimate(), delta);
		job.setStatus(Status.VALIDATION_FAIL);
		assertEquals(100.0, job.getProgressEstimate(), delta);
		job.setStatus(null);
		assertEquals(0.0, job.getProgressEstimate(), delta);
	}
	
}