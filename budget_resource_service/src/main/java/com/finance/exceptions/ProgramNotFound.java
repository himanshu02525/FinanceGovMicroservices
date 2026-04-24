package com.finance.exceptions;

@SuppressWarnings("serial")
public class ProgramNotFound extends RuntimeException{
	public ProgramNotFound(String message) 
	{
		super(message);
	}
	

}
