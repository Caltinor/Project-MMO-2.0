package harmonised.pmmo.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.util.ScreenShotHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenshotHandler
{
    public static final Logger LOGGER = LogManager.getLogger();

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static final File pmmoDir = new File( "screenshots/pmmo" );

    public static void takeScreenshot( String screenshotName, String folderName )
    {
        try
        {
            File screenshotDir = new File( pmmoDir, folderName );
            screenshotDir.mkdirs();
            Minecraft mc = Minecraft.getInstance();
            NativeImage nativeImage = ScreenShotHelper.createScreenshot(mc.getFramebuffer().framebufferWidth, mc.getFramebuffer().framebufferHeight, mc.getFramebuffer());
            String screenshotDate = DATE_FORMAT.format( new Date() );
            File screenshotFile = new File( screenshotDir, screenshotName + " " + screenshotDate + ".png" );

            nativeImage.write( screenshotFile );

//            int i = 1;
//
//            while(true)
//            {
//                File file1 = new File(gameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");
//                if (!file1.exists())
//                {
//                    return file1;
//                }
//
//                ++i;
//            }
        }
        catch ( Exception err )
        {
            LOGGER.info( "PMMO: FAILED TO TAKE SCREENSHOT", err );
        }
    }
}
