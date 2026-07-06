package com.tricongeophysics;

public class MapperToFocusException extends Exception
{

    private String message;

    public MapperToFocusException(String message)
    {
        super();
        this.message = message;
    }
    
    @Override
    public
    String getMessage() {
        return message;
    }

}
