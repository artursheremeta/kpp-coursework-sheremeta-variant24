package ua.edu.nupp.kpp.client.task;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import javafx.concurrent.Task;
import ua.edu.nupp.kpp.client.model.Exhibit;
import ua.edu.nupp.kpp.client.net.HttpMuseumClient;

/** Import every CSV row in a worker thread; progress/message are marshalled by JavaFX Task. */
public final class ImportExhibitsTask extends Task<Integer> {
    private static final Logger LOG=Logger.getLogger(ImportExhibitsTask.class.getName());
    private final Path path;
    private final HttpMuseumClient api;
    private final List<String> errors=new ArrayList<>();
    private int skipped;

    public ImportExhibitsTask(Path path,HttpMuseumClient api) {this.path=path;this.api=api;}
    public int getSkipped() {return skipped;}
    public List<String> getErrors() {return List.copyOf(errors);}

    @Override protected Integer call() throws Exception {
        updateMessage("Зчитування каталогу...");
        List<String> lines=Files.readAllLines(path,StandardCharsets.UTF_8);
        if(lines.isEmpty())throw new IllegalArgumentException("CSV-файл порожній");
        String header=lines.getFirst().replace("\uFEFF","").trim();
        String expected="inventory_number;title;material;location;sensitivity_score;exhibition_score";
        if(!header.equals(expected))throw new IllegalArgumentException("Невірний заголовок CSV. Очікується: "+expected);
        int total=(int)lines.stream().skip(1).filter(s->!s.isBlank()).count();
        int imported=0,processed=0;
        if(total==0){updateProgress(1,1);updateMessage("Немає даних для імпорту");return 0;}
        updateProgress(0,total);
        for(int row=1;row<lines.size();row++) {
            if(isCancelled())break;
            String line=lines.get(row);
            if(line.isBlank())continue;
            processed++;
            try {
                String[] p=line.split(";",-1);
                if(p.length!=6)throw new IllegalArgumentException("Очікується 6 полів, отримано "+p.length);
                Exhibit e=new Exhibit(0,p[0].trim(),p[1].trim(),p[2].trim(),p[3].trim(),
                                      Integer.parseInt(p[4].trim()),Integer.parseInt(p[5].trim()));
                api.addExhibit(e);
                imported++;
            }catch(Exception ex) {
                skipped++;
                String msg="Рядок "+(row+1)+": "+ex.getMessage();
                LOG.warning(msg);
                if(errors.size()<15)errors.add(msg);
                if(ex instanceof InterruptedException){Thread.currentThread().interrupt();break;}
            }
            updateProgress(processed,total);
            updateMessage("Оброблено "+processed+"/"+total+"; додано "+imported+"; пропущено "+skipped);
        }
        return imported;
    }
}
