/*$Id: MacrosLauncher.java,v 1.8 2012/11/19 06:59:27 Kulpanov Exp $*/
package ru.autogenmash.macros.cometmacros;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import ru.autogenmash.core.utils.StringUtils;
import ru.autogenmash.gui.AGMStyledSWTForm;
/**
 * @author Dymarchuk Dmitry
 * @version
 * refactor 27.08.2007 11:47:59
 */
public class MacrosLauncher extends Composite
{
	/** Высота заголовка в процентах от высоты экрана. */
	public static final int CAPTION_HEIGHT = 5;

    public static final String MANIFEST_ATTR_DESCRIPTION = "Description";
    public static final String MANIFEST_ATTR_IMAGE = "Image";

    public static final String COMMON_MACROSES_FILE_NAME = "BaseMacros.jar";
    public static final String ADDITIONAL_MACROSES_FILE_NAME = "AdditionalMacros.jar";

    public static final int BUTTON_SIZE = 150;
    public static final int PANELS_COUNT = 2;

    protected static Display _display;
	protected static Shell _shell;

	protected ArrayList _baseMacroses = new ArrayList(10);
	protected ArrayList _additionalMacroses = new ArrayList(10);

	protected int _currentPanelIndex;
	protected int _currentItemIndex;
    protected int _prevCurrentItemIndex;

	protected int _commonMacrosesCount;
	protected int _additionalMacrosesCount;

	protected int _panelsCount;
	protected int _compositeSize;
	protected Point _formSize;
	protected int _buttonCountPerLine;
	protected int _linesCommon;
	protected int _linesAdditional;
	protected int _expandItemHeight;
	protected Color _activeItem;
	protected Color _inActiveItem;

	protected Composite _compositeMain;
	protected Label _labelCaption;
	protected ExpandBar _expandBar;
	protected ExpandItem _expandItemCommon;
	protected ScrolledComposite _scrolledCompositeCommon;
	protected ScrolledComposite _scrolledCompositeAdditional;
	protected Composite _compositeCommon;
	protected ExpandItem _expandItemAdditional;
	protected Composite _compositeAdditional;

	protected Listener _keyDownListener;
	protected boolean _isActive = false;


    /**Возвращает количество *.class файлов в заданном JAR файле.
     * @param jarFileName
     * @return
     * @throws IOException
     */
    public static int getClassCountInJar(String jarFileName) throws IOException
    {
        int classCount = 0;
        JarFile jarFile = new JarFile(jarFileName);
        Enumeration entries  = jarFile.entries();
        while(entries.hasMoreElements())
        {
            JarEntry jarEntry = (JarEntry)entries.nextElement();
            if (jarEntry.getName().endsWith(".class"))
            {
                Attributes attributes = jarEntry.getAttributes();
                if (attributes != null)
                {
                    String imagePath = attributes.getValue(MANIFEST_ATTR_IMAGE);
                    String description = attributes.getValue(MANIFEST_ATTR_DESCRIPTION);

                    if (imagePath != null && description != null)
                        classCount++;
                }
            }
        }

        return classCount;
    }

    public MacrosLauncher(Composite parent, int style) throws IOException
    {
        super(parent, style);
        _panelsCount = PANELS_COUNT;
        _compositeSize = BUTTON_SIZE;
        _currentPanelIndex = 0;
        _currentItemIndex = 0;
        _commonMacrosesCount = getClassCountInJar(COMMON_MACROSES_FILE_NAME);
        _additionalMacrosesCount = getClassCountInJar(ADDITIONAL_MACROSES_FILE_NAME);
        _keyDownListener = new Listener()
        {
            public void handleEvent(Event event) {
                displayOnKeyDown(event);
            }
        };
    }

    /**Прочитать ресурсы и классы конкретных макросов из предопределенных jar файлов.
     * @throws IOException
     *
     */
    public ArrayList readResourses(String jarFileName, Composite composite) throws IOException
    {
        ArrayList list = new ArrayList(10);
        JarFile jarFile = new JarFile(jarFileName);
        Enumeration entries  = jarFile.entries();
        while(entries.hasMoreElements())
        {
            JarEntry jarEntry = (JarEntry)entries.nextElement();
            if (jarEntry.getName().endsWith(".class") == false)
                continue;
            Attributes attributes = jarEntry.getAttributes();
            if (attributes != null)
            {
                String imagePath = attributes.getValue(MANIFEST_ATTR_IMAGE);
                //String description = attributes.getValue(MANIFEST_ATTR_DESCRIPTION);
//                {
//                    ByteToCharCp1252 b2c = new ByteToCharCp1252();
//                    System.out.println(b2c.convertAll(description.getBytes("UTF-8")));
//                }

                if (imagePath == null /*|| description == null*/)
                {
                    // Achtung!
                    System.err.println("Warning! Image or description manifest" +
                            " info is missing in " + jarEntry.getName());
                    continue;
                }
                JarEntry imageEntry = new JarEntry(imagePath);
                if (imageEntry == null)
                {
                    System.out.println("Warning! null image entry");
                    continue;
                }
                InputStream imageStream = jarFile.getInputStream(imageEntry);
                if (imageStream == null)
                {
                    System.out.println("Warning! null image path");
                    continue;
                }
                Image image = new Image(Display.getCurrent(), imageStream);
                MacrosWidget macrosWidget = new MacrosWidget(composite,
                        StringUtils.split(jarEntry.getName(), ".")[0].replace('/', '.'),
                        /*description,*/ _compositeSize, image);
                macrosWidget.addMouseListener(new MouseAdapter()
                {
                    public void mouseDown(MouseEvent event)
                    {
                        onMacrosClick(event);
                    }
                });
                list.add(macrosWidget);
            }
            else
                System.out.println("Warning! Manifest attributes" +
                        " is missing for " + jarEntry.getName());
        }

        return list;
    }

	public void open() throws IOException
    {
	    _display.addFilter(SWT.KeyDown, _keyDownListener);

      _shell.addShellListener(new ShellAdapter() {
          public void shellDeactivated(ShellEvent arg0)
          {
              _isActive = false;
          }

          public void shellActivated(ShellEvent arg0)
          {
              _isActive = true;
          }
      });

      _shell.setLayout(new FillLayout());
      _shell.layout();

      createContents();

      Point size = getSize();
      _shell.setLayout(new FillLayout());
      _shell.layout();
      if(size.x == 0 && size.y == 0)
      {
      	pack();
          _shell.pack();
      }
      else
      {
          Rectangle shellBounds = _shell.computeTrim(0, 0, size.x, size.y);
          _shell.setSize(shellBounds.width, shellBounds.height);
      }

      _shell.open();

      _display = Display.getDefault();

      if (_commonMacrosesCount > 0)
      {
          setActiveItem(0);
      }

      while (!_shell.isDisposed())
      {
      	if (!_display.readAndDispatch())
			_display.sleep();
		}
		_display.dispose ();
	}

	protected void createContents() throws IOException
    {
		_activeItem = AGMStyledSWTForm.COLOR_RED;
		_inActiveItem = AGMStyledSWTForm.COLOR_BLUE;

		setSize(_display.getBounds().width, _display.getBounds().height);

		FillLayout fillLayout = new FillLayout();
		setLayout(fillLayout);

		_compositeMain = new Composite(this, SWT.NONE);
		_compositeMain.setLayout(new FormLayout());
		FormData formData = new FormData();
		formData.bottom = new FormAttachment(0, 692);
		formData.top = new FormAttachment(0, 0);
		formData.right = new FormAttachment(0, 664);
		formData.left = new FormAttachment(0, 0);
		_compositeMain.setLayoutData(formData);
		_compositeMain.setForeground(AGMStyledSWTForm.COLOR_DARK_BLUE);
		_compositeMain.setBackground(AGMStyledSWTForm.COLOR_BLUE);

    _labelCaption = new Label(_compositeMain, SWT.CENTER | SWT.BORDER);
    final FormData formData_2 = new FormData();
    formData_2.top = new FormAttachment(0, 0);
    formData_2.bottom = new FormAttachment(CAPTION_HEIGHT, 0);
    formData_2.left = new FormAttachment(0, 0);
    formData_2.right = new FormAttachment(100, 0);
    _labelCaption.setLayoutData(formData_2);
    initCaptionFont();
    _labelCaption.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);

		_expandBar = new ExpandBar(_compositeMain, SWT.V_SCROLL | SWT.BORDER);
		FormData formData_1 = new FormData();
		formData_1.top = new FormAttachment(CAPTION_HEIGHT, 0);
		formData_1.bottom = new FormAttachment(100, 0);
		formData_1.left = new FormAttachment(0, 0);
		formData_1.right = new FormAttachment(100, 0);
		_expandBar.setLayoutData(formData_1);
		_expandBar.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
		_expandBar.setSpacing(5);
		_expandBar.addExpandListener(new ExpandListener()
        {
            public void itemExpanded(ExpandEvent event)
            {
                if (event.item == _expandItemCommon)
                {
                    disactiveAll(_additionalMacroses);
                    _expandItemAdditional.setExpanded(false);
                    _currentPanelIndex = 0;
                    
                }
                else
                {
                    disactiveAll(_baseMacroses);
                    _expandItemCommon.setExpanded(false);
                    _currentPanelIndex = 1;
                }
                
                setActiveItem(0);
            }
            
            public void itemCollapsed(ExpandEvent event)
            {
                _prevCurrentItemIndex = _currentItemIndex = 0;
            }
        });

		_expandItemCommon = new ExpandItem(_expandBar, SWT.NONE);
		_expandItemCommon.setExpanded(true);
		_expandItemCommon.setText("Основные макросы (PgUp)");

		_expandItemAdditional = new ExpandItem(_expandBar, SWT.NONE);
		_expandItemAdditional.setText("Дополнительные макросы (PgDn)");



    _scrolledCompositeAdditional = new ScrolledComposite(_expandBar, SWT.V_SCROLL | SWT.BORDER);
    _scrolledCompositeAdditional.setLayout(new FillLayout());
    _scrolledCompositeAdditional.setBackground(AGMStyledSWTForm.COLOR_BLUE);
    _expandItemAdditional.setControl(_scrolledCompositeAdditional);

    _compositeAdditional = new Composite(_scrolledCompositeAdditional, SWT.NONE);
		_compositeAdditional.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		_compositeAdditional.setLayout(new FormLayout());

    _scrolledCompositeAdditional.setContent(_compositeAdditional);

		_scrolledCompositeCommon = new ScrolledComposite(_expandBar, SWT.V_SCROLL | SWT.BORDER);
		_scrolledCompositeCommon.setLayout(new FillLayout());
    _scrolledCompositeCommon.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		_expandItemCommon.setControl(_scrolledCompositeCommon);

		_compositeCommon = new Composite(_scrolledCompositeCommon, SWT.NONE);
		_compositeCommon.setLocation(0, 0);
		_compositeCommon.setLayout(new FormLayout());

		_compositeCommon.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		_scrolledCompositeCommon.setContent(_compositeCommon);

		_expandItemHeight = (int)Math.round(this.getSize().y * (100 - CAPTION_HEIGHT)/100) - _expandBar.getItemCount()*(_expandBar.getSpacing() + _expandBar.getItem(0).getHeaderHeight()) - 10;
		_expandItemCommon.setHeight(_expandItemHeight);
		_expandItemAdditional.setHeight(_expandItemHeight);

		_expandBar.getItem(_currentPanelIndex).setExpanded(true);



		this.layout();

    _formSize = new Point(_scrolledCompositeCommon.getBounds().width, _scrolledCompositeCommon.getBounds().height);//this.getSize();
    _buttonCountPerLine = _formSize.x / _compositeSize;

    _compositeSize = (int)Math.ceil((double)_formSize.x / _buttonCountPerLine) - 1;

    _linesCommon = (int)Math.ceil((double)_commonMacrosesCount/_buttonCountPerLine);
    _linesAdditional = (int)Math.ceil((double)_additionalMacrosesCount/_buttonCountPerLine);

    _compositeAdditional.setSize(_formSize.x, _linesAdditional * _compositeSize + 0);
    _compositeCommon.setSize(_formSize.x, _linesCommon * _compositeSize + 0);
    _baseMacroses = readResourses(COMMON_MACROSES_FILE_NAME, _compositeCommon);
    _additionalMacroses = readResourses(ADDITIONAL_MACROSES_FILE_NAME, _compositeAdditional);
    locateMacrosWidgets(_baseMacroses);
    locateMacrosWidgets(_additionalMacroses);
	}

    protected void initCaptionFont()
    {
        Font captionFont;
        if (_display.getBounds().width > 1200)
            captionFont = AGMStyledSWTForm.FONT_20B;
        else if (_display.getBounds().width > 1000)
            captionFont = AGMStyledSWTForm.FONT_14B;
        else
            captionFont = AGMStyledSWTForm.FONT_12B;
        _labelCaption.setFont(captionFont);
    }

    /**Расположить виджеты макросов на соответствующих вкладках.
     * @param list
     */
    protected void locateMacrosWidgets(ArrayList list)
    {
        int k = 0;
    		for(int i = 0; i <  (int)Math.ceil((double)list.size()/_buttonCountPerLine); i++)
    		{
    			for(int j = 0; j < _buttonCountPerLine; j++)
    			{
    				((MacrosWidget)list.get(k)).setLocation(_compositeSize * j, _compositeSize * i);
    				k++;
    				if( k == list.size() )
    					break;
    			}
    		}
    }

	protected void exit()
    {
		_display.dispose();
	}

	protected void displayOnKeyDown(Event event)
    {
        if (_isActive == false)
            return;

		_prevCurrentItemIndex = _currentItemIndex;

		
  switch(event.keyCode)
  {
		case SWT.PAGE_UP:
			if( (_currentPanelIndex > 0) && (_currentPanelIndex < _panelsCount) )
            {
                setActiveItem(0);
				_expandBar.getItem(_currentPanelIndex).setExpanded(false);
				_currentPanelIndex -= 1;
				_expandBar.getItem(_currentPanelIndex).setExpanded(true);
                _prevCurrentItemIndex = 0;
                setActiveItem(0);
			}
			break;
		case SWT.PAGE_DOWN:
			if( (_currentPanelIndex >= 0) && (_currentPanelIndex < _panelsCount - 1) )
            {
                setActiveItem(0);
				_expandBar.getItem(_currentPanelIndex).setExpanded(false);
				_currentPanelIndex += 1;
				_expandBar.getItem(_currentPanelIndex).setExpanded(true);
                _prevCurrentItemIndex = 0;
                setActiveItem(0);
			}
            break;
		case SWT.ARROW_UP:
		case SWT.ARROW_DOWN:
		case SWT.ARROW_LEFT:
		case SWT.ARROW_RIGHT:
            processArrows(event.keyCode);
            break;
		case 13: // Enter
		case SWT.KEYPAD_CR:
            launch();
			break;
		case 'q':
            if (event.stateMask == SWT.CTRL)
                exit();
        case SWT.ESC:
            exit();
		}
	}

    private void launch()
    {
        if (_currentPanelIndex == 0)
            ((MacrosWidget)_baseMacroses.get(_currentItemIndex)).loadMacros();
        else
            ((MacrosWidget)_additionalMacroses.get(_currentItemIndex)).loadMacros();
    }

    protected void processArrows(int keyCode)
    {
        int macrosCount;
        if (_currentPanelIndex == 0)
            macrosCount = _commonMacrosesCount;
        else
            macrosCount = _additionalMacrosesCount;

        switch (keyCode)
        {
        case SWT.ARROW_UP:
            if(_currentItemIndex - _buttonCountPerLine >= 0)
                _currentItemIndex -= _buttonCountPerLine;
            break;
        case SWT.ARROW_DOWN:
            if(_currentItemIndex + _buttonCountPerLine <= macrosCount - 1)
                _currentItemIndex += _buttonCountPerLine;
            break;
        case SWT.ARROW_LEFT:
            if(_currentItemIndex - 1 >= 0)
                _currentItemIndex -= 1;
            break;
        case SWT.ARROW_RIGHT:
            if(_currentItemIndex + 1 <= macrosCount - 1)
                _currentItemIndex += 1;
            break;
        }
        setActiveItem(_currentItemIndex);
    }

    private void disactiveAll(List macroses)
    {
        for (int i = 0; i < macroses.size(); i++)
        {
            MacrosWidget macrosWidget = (MacrosWidget)macroses.get(i);
            macrosWidget.setBackground(_inActiveItem);
        }
    }
    
    protected void setActiveItem(int currentItemIndex)
    {
        _currentItemIndex = currentItemIndex;
        ArrayList macroses;
        if (_currentPanelIndex == 0)
            macroses = _baseMacroses;
        else
            macroses = _additionalMacroses;
        
        ((MacrosWidget)macroses.get(_prevCurrentItemIndex)).setBackground(_inActiveItem);
        ((MacrosWidget)macroses.get(currentItemIndex)).setFocus();
        ((MacrosWidget)macroses.get(currentItemIndex)).setBackground(_activeItem);
        _labelCaption.setText(((MacrosWidget)macroses.get(currentItemIndex)).getDescription());
    }

    protected void onMacrosClick(MouseEvent event)
    {
        MacrosWidget currentMacrosWidget = (MacrosWidget)event.widget.getData("parent");
        if (findMacros(_baseMacroses, currentMacrosWidget) == false)
            findMacros(_additionalMacroses, currentMacrosWidget);
        
        launch();
    }

    private boolean findMacros(List macroses, MacrosWidget currentMacrosWidget)
    {
        for (int i = 0; i < macroses.size(); i++)
        {
            MacrosWidget macrosWidget = (MacrosWidget)macroses.get(i);
            if (macrosWidget == currentMacrosWidget)
            {
                if (macroses == _baseMacroses)
                    _currentPanelIndex = 0;
                else
                    _currentPanelIndex = 1;
                
                _prevCurrentItemIndex = _currentItemIndex;
                setActiveItem(i);
                return true;
            }
        }
        
        return false;
    }
    
/******************************************************************************/

    public static void main(String[] args)
    {
        try
        {
            _display = Display.getDefault();
            _shell = new Shell(_display, SWT.NULL);
            _shell.setMaximized(true);
            _shell.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
            _shell.setText("Загрузчик макросов");
            _shell.setLocation(0,0);
            MacrosLauncher macros = new MacrosLauncher(_shell, SWT.NULL);
            macros.open();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
