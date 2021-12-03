package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.PlayerConnectedHandler;
import harmonised.pmmo.util.XP;
import harmonised.pmmo.util.Reference;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.ScreenEvent;

import java.util.*;

public class CreditorScreen extends PmmoScreen
{
    public static final HashMap<String, String> uuidName = new HashMap<>();
        private final ResourceLocation box = XP.getResLoc(Reference.MOD_ID, "textures/gui/screenboxy.png");
    public static final Map<String, List<String>> creditorsInfo = new HashMap<>();
    public static Map<String, Integer> colors = new HashMap<>();
    private static TileButton exitButton;

    
    
    private int x;
    private int y;
    
//    private UUID uuid;
    public String playerName;
    private int lastScroll;
    private int color;
    private String transKey;

    public CreditorScreen(String playerName, String transKey, int lastScroll)
    {
        super(new TranslatableComponent(transKey));
//        this.uuid = uuid;
        this.playerName = playerName;
        this.lastScroll = lastScroll;
        this.transKey = transKey;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        tileButtons = new ArrayList<>();

        x = ((sr.getGuiScaledWidth() / 2) - (boxWidth / 2));
        y = ((sr.getGuiScaledHeight() / 2) - (boxHeight / 2));

        exitButton = new TileButton(x + boxWidth - 24, y - 8, 7, 0, "pmmo.exit", JType.NONE, (something) ->
        {
            Minecraft.getInstance().setScreen(new CreditsScreen(Minecraft.getInstance().player.getUUID(), new TranslatableComponent("pmmo.credits"), JType.CREDITS));
        });

        addRenderableWidget(exitButton);

//        for(TileButton button : tileButtons)
//        {
//            addWidget(button);
//        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks)
    {
        renderBackground(stack, 1);
        super.render(stack, mouseX, mouseY, partialTicks);

        x = ((sr.getGuiScaledWidth() / 2) - (boxWidth / 2));
        y = ((sr.getGuiScaledHeight() / 2) - (boxHeight / 2));

        color = 0xffffff;

        if(colors.containsKey(playerName))
            color = colors.get(playerName);

        drawCenteredString(stack, font, "§l" + playerName, sr.getGuiScaledWidth() / 2, y - (font.width(playerName) > 220 ? 10 : 5), color);

        List<String> list = creditorsInfo.get(playerName);

        if(list == null)
            drawCenteredString(stack, font, "§lError! Please Report me! \"" + playerName + "\"", sr.getGuiScaledWidth() / 2, sr.getGuiScaledHeight() / 2, color);
        else
        {
            for(int i = 0; i < list.size(); i++)
            {
                drawCenteredString(stack, font, (list.get(i).contains("§l") ? "" : "§l") + list.get(i), sr.getGuiScaledWidth() / 2, (sr.getGuiScaledHeight() / 2 - (list.size() * 20) / 2) + i * 20, color);
            }
        }

        RenderSystem.enableBlend();
    }

    @Override
    public void renderBackground(PoseStack stack, int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(stack, 0, 0, this.width, this.height, 0x66222222, 0x66333333);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundDrawnEvent(this, stack));
        }
        else
            this.renderBackground(stack, p_renderBackground_1_);


        boxHeight = 256;
        boxWidth = 256;
        RenderSystem.setShaderTexture(0, box);
        RenderSystem.disableBlend();
        this.blit(stack, x, y, 0, 0, boxWidth, boxHeight);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if(button == 1)
        {
            exitButton.onPress();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public static void initCreditors()
    {
        uuidName.put("6aefd519-8f6b-4c3a-bb1b-aac88a8dd57e", "Deviate#0001");
        uuidName.put("12319dec-8880-4e9b-8ddb-01719a66e793", "Starbble#4578");
        uuidName.put("e4c7e475-c1ff-4f94-956c-ac5be02ce04a", "Lucifer#0666");
        uuidName.put("8eb0578d-c113-49d3-abf6-a6d36f6d1116", "Tyrius#0842");
        uuidName.put("2ea5efa1-756b-4c9e-9605-7f53830d6cfa", "didis54#5815");
        uuidName.put("0bc51f06-9906-41ea-9fb4-7e9be169c980", "stressindicator#8819");
        uuidName.put("5bfdb948-7b66-476a-aefe-d45e4778fb2d", "Daddy_P1G#0432");
        uuidName.put("554b53b8-d0fa-409e-ab87-2a34bf83e506", "joerkig#1337");
        uuidName.put("21bb554a-f339-48ef-80f7-9a5083172892", "Judicius#1036");
        uuidName.put("edafb5eb-9ccb-4121-bef7-e7ffded64ee3", "Lewdcina#0001");
        uuidName.put("8d2460f3-c840-4b8e-a2d2-f7d5168cbdeb", "qSided#0420");
        List<String> list;

        /////////LAVENDER//////////////
        PlayerConnectedHandler.lavenderPatreons.forEach(a ->
        {
            colors.put(uuidName.get(a.toString()), 0xd200ff);
            creditorsInfo.put(uuidName.get(a.toString()), new ArrayList<>());
        });
        //DEVIATE
        list = creditorsInfo.get("Deviate#0001");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "04/09/2021").getString());
        //STARBBLE
        list = creditorsInfo.get("Starbble#4578");
        list.add("First Lavender Tier Patreon");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "05/05/2020").getString());
        /////////FIERY//////////////
        PlayerConnectedHandler.fieryPatreons.forEach(a ->
        {
            colors.put(uuidName.get(a.toString()), 0xf97900);
            creditorsInfo.put(uuidName.get(a.toString()), new ArrayList<>());
        });
        /////////LAPIS//////////////
        PlayerConnectedHandler.lapisPatreons.forEach(a ->
        {
            colors.put(uuidName.get(a.toString()), 0x5555ff);
            creditorsInfo.put(uuidName.get(a.toString()), new ArrayList<>());
        });
        //LUCIFER
        list = creditorsInfo.get("Lucifer#0666");
        list.add("First Lapis Tier Patreon");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "28/04/2020").getString());

        /////////DANDELION//////////
        //TYRIUS
        PlayerConnectedHandler.dandelionPatreons.forEach(a ->
        {
            colors.put(uuidName.get(a.toString()), 0xffff33);
            creditorsInfo.put(uuidName.get(a.toString()), new ArrayList<>());
        });
        list = creditorsInfo.get("Tyrius#0842");
        list.add("First Dandelion Tier Patreon");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "19/03/2020").getString());
        list.add(new TranslatableComponent("pmmo.creatorOfModpack", "The Cosmic Tree").getString());
        list.add(new TranslatableComponent("pmmo.helpedFillingInModValues", "Botania").getString());

        list = creditorsInfo.get("joerkig#1337");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "3/11/2020").getString());

        list = creditorsInfo.get("Judicius#1036");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "22/11/2020").getString());

        //DIDIS54
        list = creditorsInfo.get("didis54#5815");
        list.add("First Iron Tier Patreon");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "11/04/2020").getString());
        list.add(new TranslatableComponent("pmmo.creatorOfModpack", "Anarkhe Revolution").getString());
        list.add(new TranslatableComponent("pmmo.helpedTranslating", "French").getString());

        /////////IRON///////////////
        PlayerConnectedHandler.ironPatreons.forEach(a ->
        {
            colors.put(uuidName.get(a.toString()), 0xeeeeee);
            creditorsInfo.put(uuidName.get(a.toString()), new ArrayList<>());
        });
        list = creditorsInfo.get("qSided#0420");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "23/08/2020").getString());

        //STRESSINDICATOR
        list = creditorsInfo.get("stressindicator#8819");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "17/08/2020").getString());

        //DADDY_P1G
        list = creditorsInfo.get("Daddy_P1G#0432");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "29/06/2020").getString());

        //NEOTHIAMIN
        creditorsInfo.put("neothiamin#1798", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "17/04/2020").getString());
            add(new TranslatableComponent("pmmo.creatorOfModpack", "Skillful Survival").getString());
        }});

        //DARTH_REVAN#7341
        creditorsInfo.put("Darth Revan#7341", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "17/04/2020").getString());
            add(new TranslatableComponent("pmmo.creatorOfModpack", "Zombie Textiles").getString());
        }});

        //LEWDCINA
        list = creditorsInfo.get("Lewdcina#0001");
        list.add(new TranslatableComponent("pmmo.discordMemberSince", "07/07/2020").getString());

        /////////TRANSLATOR/////////
        //BusanDaek#3970
        creditorsInfo.put("BusanDaek#3970", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "31/03/2020").getString());
            add(new TranslatableComponent("pmmo.translated", "Korean").getString());
        }});
        //deezer911#5693
        creditorsInfo.put("deezer911#5693", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "11/03/2020").getString());
            add(new TranslatableComponent("pmmo.helpedTranslating", "French").getString());
        }});
        //Dawnless#1153
        creditorsInfo.put("Dawnless#1153", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "22/08/2020").getString());
            add(new TranslatableComponent("pmmo.translated", "Dutch - Netherlands").getString());
        }});
        //TorukM4kt00#0246
        creditorsInfo.put("TorukM4kt00#0246", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "13/05/2020").getString());
            add(new TranslatableComponent("pmmo.translated", "Portuguese - Brazil").getString());
        }});
        //starche#7569
        creditorsInfo.put("starche#7569", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "24/07/2020").getString());
            add(new TranslatableComponent("pmmo.translated", "Russian").getString());
        }});
        //Lyla#2639
        creditorsInfo.put("Lyla#2639", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "28/10/2020").getString());
            add(new TranslatableComponent("pmmo.translated", "Chinese Traditional").getString());
            add(new TranslatableComponent("pmmo.translated", "Chinese Simplified").getString());
        }});
        //Matterfall#1952
        creditorsInfo.put("Matterfall#1952", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "22/11/2020").getString());
            add(new TranslatableComponent("pmmo.translated", "German").getString());
        }});
        //N1co#9248
        creditorsInfo.put("N1co#9248", new ArrayList<String>()
        {{
            add(new TranslatableComponent("pmmo.discordMemberSince", "25/11/2020").getString());
            add(new TranslatableComponent("pmmo.translated", "Spanish").getString());
        }});
    }
}