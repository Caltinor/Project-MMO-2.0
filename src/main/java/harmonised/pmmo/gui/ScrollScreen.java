package harmonised.pmmo.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import harmonised.pmmo.config.JsonConfig;
import harmonised.pmmo.skills.Skill;
import harmonised.pmmo.skills.XP;
import harmonised.pmmo.util.Reference;
import it.unimi.dsi.fastutil.ints.IntComparators;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.gui.ScrollPanel;

import java.util.*;

public class ScrollScreen extends Screen
{
    private final List<IGuiEventListener> children = Lists.newArrayList();
    private final ResourceLocation box = new ResourceLocation( Reference.MOD_ID, "textures/gui/screenboxy.png" );

    MainWindow sr = Minecraft.getInstance().getMainWindow();;
    private int boxWidth = 256;
    private int boxHeight = 256;
    private int x, y, scrollX, scrollY, buttonX, buttonY;
    private Button exitButton;
    private MyScrollPanel scrollPanel;
    private PlayerEntity player;
    private String type;
    private ArrayList<ListButton> tempList;
    private ArrayList<ListButton> listButtons = new ArrayList<>();

    public ScrollScreen( ITextComponent titleIn, String type, PlayerEntity player )
    {
        super(titleIn);
        this.player = player;
        this.type = type;
    }

//    @Override
//    public boolean isPauseScreen()
//    {
//        return false;
//    }

    @Override
    protected void init()
    {
        ArrayList<String> keyWords = new ArrayList<>();
        keyWords.add( "helmet" );
        keyWords.add( "chestplate" );
        keyWords.add( "leggings" );
        keyWords.add( "boots" );
        keyWords.add( "pickaxe" );
        keyWords.add( "axe" );
        keyWords.add( "shovel" );
        keyWords.add( "hoe" );
        keyWords.add( "sword" );

        x = (sr.getScaledWidth() / 2) - (boxWidth / 2);
        y = (sr.getScaledHeight() / 2) - (boxHeight / 2);
        scrollX = x + 16;
        scrollY = y + 10;

        exitButton = new TileButton( x + boxWidth - 24, y - 8, 0, 2, I18n.format("" ), (something) ->
        {
            Minecraft.getInstance().displayGuiScreen( new SkillsScreen( new TranslationTextComponent( "pmmo.skills" ) ) );
        });

        Map<String, Map<String, Object>> reqs = XP.getFullReqMap( type );

        tempList = new ArrayList<>();
        listButtons = new ArrayList<>();

        for( Map.Entry<String, Map<String, Object>> entry : reqs.entrySet() )
        {
            if( XP.getItem( entry.getKey() ) != Items.AIR )
            {
                tempList.add( new ListButton( scrollX + boxWidth - 86, 0, 1, entry.getKey(), type, a ->
                {
                    System.out.println( "clicc" );
                }));
            }
            else if( type.equals( "biome" ) )
            {
                tempList.add( new ListButton( scrollX + boxWidth - 86, 0, 0, entry.getKey(), type, a ->
                {
                    System.out.println( "clicc" );
                }));
            }
        }

        for( String keyWord : keyWords )
        {
            for( ListButton a : tempList )
            {
                if( a.regKey.contains( keyWord ) )
                {
                    if( !listButtons.contains(a) )
                        listButtons.add( a );
                }
            }
        }

        for( ListButton a : tempList )
        {
            if( !listButtons.contains( a ) )
                listButtons.add( a );
        }

        int accummulativeHeight = 4;

        for( ListButton a : listButtons )
        {
            for( Map.Entry<String, Object> inEntry : reqs.get( a.regKey ).entrySet() )
            {
                if( Skill.getSkill( inEntry.getKey() ).getLevel( player ) < (int) (double) inEntry.getValue() )
                    a.text.add( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + inEntry.getKey() ), (int) (double) inEntry.getValue() ).setStyle( XP.textStyle.get( "red" ) ) );
                else
                    a.text.add( new TranslationTextComponent( "pmmo.levelDisplay", new TranslationTextComponent( "pmmo." + inEntry.getKey() ), (int) (double) inEntry.getValue() ).setStyle( XP.textStyle.get( "green" ) ) );
            }

            a.text.sort( Comparator.comparingInt(ScrollScreen::getTextLevel).reversed() );
        }

        listButtons.sort( Comparator.comparingInt( b -> XP.getHighestReq( b.itemStack.getItem().getRegistryName().toString(), type) ) );

        scrollPanel = new MyScrollPanel( Minecraft.getInstance(), boxWidth - 42, boxHeight - 21, scrollY, scrollX, type, player, listButtons );

        children.add( scrollPanel );

        addButton( exitButton );
    }

    private static int getTextLevel( ITextComponent comp )
    {
        String number = comp.getUnformattedComponentText().replaceAll("\\D+","");

        if( !Double.isNaN( Double.parseDouble( number ) ) )
            return (int) Double.parseDouble( number );
        else
            return 0;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks)
    {
        renderBackground( 1 );

        drawCenteredString( font, title.getFormattedText(), sr.getScaledWidth() / 2, y - 5, 0xffffff );

        x = ( (sr.getScaledWidth() / 2) - (boxWidth / 2) );
        y = ( (sr.getScaledHeight() / 2) - (boxHeight / 2) );
        scrollX = x + 16;
        scrollY = y + 10;
        buttonX = scrollX + 4;

        scrollPanel.render( mouseX, mouseY, partialTicks );

        int startI = (int) Math.floor( scrollPanel.getScroll() / 36D );
        if( startI < 0 )
            startI = 0;

        ListButton button;

        for( int i = startI; i < startI + 7; i++ )
        {
            if( listButtons.size() - 1 >= i )
            {
                button = listButtons.get( i );
                buttonX = mouseX - button.x;
                buttonY = mouseY - button.y;

                if( button.element != 0 && buttonX >= 0 && buttonX < 32 && buttonY >= 0 && buttonY < 32 )
                    renderTooltip( button.itemStack, mouseX, mouseY );
            }
        }

//        renderTooltip( mouseX + " " + mouseY, mouseX, mouseY );
//        drawCenteredString(Minecraft.getInstance().fontRenderer, player.getDisplayName().getString() + " " + type,x + boxWidth / 2, y + boxHeight / 2, 50000 );
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderBackground(int p_renderBackground_1_)
    {
        if (this.minecraft != null)
        {
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent(this));
        }

        boxHeight = 256;
        boxWidth = 256;
        Minecraft.getInstance().getTextureManager().bindTexture( box );

        this.blit( x, y, 0, 0,  boxWidth, boxHeight );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
    {
        scrollPanel.mouseScrolled( mouseX, mouseY, scroll );
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseClicked( mouseX, mouseY, button );
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        scrollPanel.mouseReleased( mouseX, mouseY, button );
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        scrollPanel.mouseDragged( mouseX, mouseY, button, deltaX, deltaY );
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

}