import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class NoteManager {

    private Note n;
    private String filename;
    private String noteRetrieval;

    public NoteManager() {

    }

    public NoteManager(String filePath) {
        this.filename = filePath;
    }

    public void setNote(Note userNote) {
        this.n = userNote;
    }

    public Note getNote() {
        return this.n;
    }

    public void saveNote() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true));
        writer.write(n.toString() + "\n\n");
        writer.close();
    }

    public void loadNote() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filename));

        String line;
        String accumulator = "";
        while((line = reader.readLine()) != null) {
            accumulator += line + "\n";
            
            //System.out.println(line);
        }
        this.noteRetrieval = accumulator;
        
        reader.close();
        
    }

    public String getLoadedNote() {
        return this.noteRetrieval;
    }
    
}


