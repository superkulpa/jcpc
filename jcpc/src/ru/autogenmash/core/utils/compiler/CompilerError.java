package ru.autogenmash.core.utils.compiler;

/**
 * @author Dymarchuk Dmitry
 * 18.03.2009 13:20:58
 */
public class CompilerError
{
    private int _shapeNumber = -1;

    private int _frameNumber = -1;

    private String _message;

    public CompilerError(int shapeNumber, int frameNumber, String message)
    {
        super();
//        if (message.trim().equals(""))
//            throw new IllegalArgumentException("message can not be empty");

        _shapeNumber = shapeNumber;
        _frameNumber = frameNumber;
        _message = message;
    }

    public CompilerError(int frameNumber, String message)
    {
        this(-1, frameNumber, message);
    }
    
    public CompilerError(int frameNumber)
    {
        this(-1, frameNumber, "");
    }

    public CompilerError(String message)
    {
        this(-1, -1, message);
    }

    public int getFrameNumber()
    {
        return _frameNumber;
    }

    public void setFrameNumber(int frameNumber)
    {
        _frameNumber = frameNumber;
    }

    public String getMessage()
    {
        return _message;
    }

    public void setMessage(String message)
    {
        _message = message;
    }

    
    public int getShapeNumber()
    {
        return _shapeNumber;
    }

    public void setShapeNumber(int shapeNumber)
    {
        _shapeNumber = shapeNumber;
    }

    public String toString()
    {
        if (_message == null)
            return "";

        String result = "";

        if (_shapeNumber >= 0)
            result += " в контуре №" + _shapeNumber;

        if (_frameNumber >= 0)
            result += " в кадре №" + _frameNumber;

        result += ": " + _message;

        return result;
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof CompilerError == false)
            throw new IllegalArgumentException("object \"" + obj +
                    "\" must be instance of " + CompilerError.class.getName());

        CompilerError ce = (CompilerError)obj;
        if (ce._shapeNumber == _shapeNumber &&
            ce._frameNumber == _frameNumber &&
            ce._message.equals(_message))
            return true;

        return false;
    }
}

















