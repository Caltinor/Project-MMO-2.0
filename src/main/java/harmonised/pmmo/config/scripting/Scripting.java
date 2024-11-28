package harmonised.pmmo.config.scripting;

import harmonised.pmmo.util.MsLoggy;
import net.minecraft.core.RegistryAccess;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Scripting {
    public static void readFiles(RegistryAccess access) {
        Path filePath = FMLPaths.CONFIGDIR.get();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(filePath, "*.pmmo")) {
            for (Path path : stream) {
                MsLoggy.INFO.log(MsLoggy.LOG_CODE.API, "Loading script from {}", path);
                read(access, new String(Files.readAllBytes(path)));
            }
        } catch (IOException e) {e.printStackTrace();}
    }

    public static void read(RegistryAccess access, String rawString) {
        read(access, rawString.lines().filter(str -> !str.startsWith("//")).toList());
    }

    public static void read(RegistryAccess access, List<String> lines) {
        String currentNode = "";
        StringBuilder multiLine = new StringBuilder();
        List<Expression> builders = new ArrayList<>();
        for (String str : lines) {
            int eolci = str.indexOf("//"); //end of line comment index
            String strippedOfComments = str.substring(0, eolci == -1 ? Math.max(0, str.length()) : eolci);
            String trimmed = strippedOfComments.replaceAll("^[ \\t]+", "").replaceAll("[ \\t]+$", "");
            if (trimmed.isEmpty()) continue;
            //is a block node
            if (trimmed.startsWith("WITH")) {
                currentNode = trimmed.replaceAll("^WITH ?", "");
                multiLine = new StringBuilder();
            }
            else if (trimmed.startsWith("END")) {
                currentNode = "";
                multiLine = new StringBuilder();
            }
            else if (!trimmed.endsWith(";"))
                multiLine.append(trimmed);
            else {
                List<Expression> exprs = Expression.create(access, currentNode + multiLine + trimmed)
                        .stream().filter(Expression::isValid).toList();
                multiLine = new StringBuilder();
                builders.addAll(exprs);
            }
        }
        builders.forEach(Expression::commit);
    }
}