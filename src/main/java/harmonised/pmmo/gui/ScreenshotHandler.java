package harmonised.pmmo.gui;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Screenshot;
import org.apache.logging.log4j.*;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static final File pmmoDir = new File("screenshots/pmmo");

    public static void takeScreenshot(String screenshotName, String folderName)
    {
        try
        {
            File screenshotDir = new File(pmmoDir, folderName);
            screenshotDir.mkdirs();
            Minecraft mc = Minecraft.getInstance();
            NativeImage nativeImage = Screenshot.takeScreenshot(mc.getMainRenderTarget());
            String screenshotDate = DATE_FORMAT.format(new Date());
            File screenshotFile = new File(screenshotDir, screenshotName + " " + screenshotDate + ".png");

            nativeImage.writeToFile(screenshotFile);
        }
        catch (Exception err)
        {
            LOGGER.error("PMMO: FAILED TO TAKE SCREENSHOT", err);
        }
    }
}
