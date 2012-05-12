/**
 * TestException
 * 13.05.2012
 * @author Philipp Haussleiter
 *
 */
public class TestException extends Exception {
    public TestException(String message){
        super(message);
    }

    @Override
    public String getLocalizedMessage(){
        return "TestException: "+super.getLocalizedMessage()+" "+System.currentTimeMillis();
    }
}
