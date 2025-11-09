import java.time.LocalDate;

public class Note {

    private String input = "";

    public Note(String userInput) {
        
        input = LocalDate.now() + "\n" + input + userInput;
    }

    public String getInput() {

        return this.input;
    }

    @Override
    public String toString() {
        return this.input;
    }
    
}