/*$Id: MacrosParametersDialog.java,v 1.2 2011/03/05 08:49:35 Kulpanov Exp $*/
package ru.autogenmash.macros.cometmacros;

import java.io.File;
import java.io.FilenameFilter;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.autogenmash.core.utils.StringUtils;
import ru.autogenmash.core.utils.XmlUtils;
import ru.autogenmash.gui.AGMStyledSWTForm;
import ru.autogenmash.gui.SWTFormBase;

/**Диалог загрузки/сохранения параметров макроса.
 * @author Dymarchuk Dmitry
 * @version
 * refactor 11.09.2007 9:17:56
 */
public class MacrosParametersDialog extends Dialog
{
    public static final String CAPTION_LOAD = "Загрузить\n(Ctrl+Enter)";
    public static final String CAPTION_DELETE = "Удалить\n(Del)";
    public static final String CAPTION_VIEW = "Просмотреть\n(Enter)";
    public static final String CAPTION_REWRITE = "Переписать\n(Ctrl+S)";
    public static final String CAPTION_SAVE = "Сохранить\n(Ctrl+S)";

    public static final int DIALOG_HEIGHT = 500;
    public static final int DIALOG_WIDTH = 605;

    public static final String XML_ROOT = "macros";

    protected List _listFileNames;
    protected List _listParams;
    protected Label _labelSave;
    protected Label _labelPreview;
    protected Label _labelDelete;
    protected Label _labelLoad;
    protected Label _labelMessage;
    protected Text _textFileName;
    protected Label _labelFileSaveCaption;
    protected Composite _compositeSave;
    protected Composite _compositeCaption;
    protected Composite _compositeMain;
    protected Composite _compositeBottomControls;

    protected Label _labelCaption;
    protected Shell _shell;

    /** Директория, в которой располагаются файлы параметров. */
    protected String _directory;
    /** Текущий файл параметров. */
    protected String _fileName;

    protected String _caption;

    protected TreeMap _outputParams;
    protected TreeMap _inputParams;


    public MacrosParametersDialog(Shell parent, int style)
    {
        super(parent, style);
    }

    public MacrosParametersDialog(Shell parent)
    {
        this(parent, SWT.NONE);
    }

    /**Открывает диалог сохранения параметров.
     * @param directory
     * @param inputParams входные параметры для сохранения в файле параметров.
     * @return
     */
    public TreeMap open(String caption, String directory, TreeMap inputParams)
    {
        _caption = caption;
        _inputParams = inputParams;
        _outputParams = null;
        _directory = directory;

        createContents();

        _shell.open();
        _shell.layout();
        Display display = getParent().getDisplay();
        if (_listFileNames.getItemCount() > 0)
            _listFileNames.setFocus();
        else
            _textFileName.setFocus();
        // FIXME bad solution! Why? I do not know!
        Point size = _compositeMain.computeSize(_compositeMain.getSize().x, _compositeMain.getSize().y);
        _compositeMain.setSize(size.x - 5, size.x - 5);
        _compositeMain.setSize(size.x - 4, size.y - 4);
        while (!_shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();
        return _outputParams;
    }

    protected void createContents()
    {
        _shell = new Shell(getParent(), SWT.APPLICATION_MODAL);
        _shell.setLayout(new FormLayout());
        _shell.setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        _shell.setLocation(_shell.getParent().getBounds().width / 2 - DIALOG_WIDTH / 2,
                _shell.getParent().getBounds().height / 2 - DIALOG_HEIGHT / 2);

        _compositeCaption = new Composite(_shell, SWT.BORDER);
        final FormData formData1 = new FormData();
        formData1.bottom = new FormAttachment(0, 35);
        formData1.top = new FormAttachment(0, 0);
        formData1.right = new FormAttachment(100, 0);
        formData1.left = new FormAttachment(0, 0);
        _compositeCaption.setLayoutData(formData1);
        _compositeCaption.setLayout(new FillLayout());

        _labelCaption = new Label(_compositeCaption, SWT.CENTER);
        _labelCaption.setFont(AGMStyledSWTForm.FONT_12B);
        _labelCaption.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        _labelCaption.setText("Файл: " + _caption);

        _compositeMain = new Composite(_shell, SWT.BORDER);
        final FormData formData2 = new FormData();
        formData2.top = new FormAttachment(_compositeCaption, 0, SWT.BOTTOM);
        formData2.right = new FormAttachment(100, 0);
        formData2.left = new FormAttachment(0, 0);
        _compositeMain.setLayoutData(formData2);
        _compositeMain.setLayout(new FormLayout());
        _compositeMain.setBackground(AGMStyledSWTForm.COLOR_BLUE);


        _listParams = new List(_compositeMain, SWT.V_SCROLL | SWT.BORDER);
        final FormData formData3 = new FormData();
        formData3.bottom = new FormAttachment(100, -66);
        formData3.right = new FormAttachment(100, 0);
        formData3.left = new FormAttachment(50, 0);
        formData3.top = new FormAttachment(0, 0);
        _listParams.setLayoutData(formData3);
        _listParams.setFont(AGMStyledSWTForm.FONT_10B);
        _listParams.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent arg0)
            {
                _listFileNames.setFocus();
            }
        });

        _compositeBottomControls = new Composite(_shell, SWT.BORDER);
        formData2.bottom = new FormAttachment(_compositeBottomControls, 0, SWT.TOP);

        _labelMessage = new Label(_compositeMain, SWT.NONE);
        final FormData formData4 = new FormData();
        formData4.right = new FormAttachment(100, 0);
        formData4.left = new FormAttachment(0, 0);
        formData4.bottom = new FormAttachment(100, 0);
        formData4.top = new FormAttachment(100, -30);
        _labelMessage.setLayoutData(formData4);
        _labelMessage.setForeground(AGMStyledSWTForm.COLOR_RED);
        _labelMessage.setFont(AGMStyledSWTForm.FONT_14);
        _labelMessage.setBackground(AGMStyledSWTForm.COLOR_BLUE);

        _listFileNames = new List(_compositeMain, SWT.V_SCROLL | SWT.BORDER);
        _listFileNames.setFont(AGMStyledSWTForm.FONT_14);
        final FormData formData5 = new FormData();
        formData5.bottom = new FormAttachment(100, -66);
        formData5.top = new FormAttachment(0, 0);
        formData5.left = new FormAttachment(0, 0);
        formData5.right = new FormAttachment(50, 0);
        _listFileNames.setLayoutData(formData5);
        _listFileNames.addKeyListener(new KeyAdapter()
        {
            public void keyPressed(KeyEvent event)
            {
                listOnKeyDown(event);
            }
        });

        readFiles();

        _compositeSave = new Composite(_compositeMain, SWT.NONE);
        _compositeSave.setLayout(new FormLayout());
        final FormData formData = new FormData();
        formData.right = new FormAttachment(100, 0);
        formData.left = new FormAttachment(0, 0);
        formData.bottom = new FormAttachment(100, -30);
        formData.top = new FormAttachment(100, -66);
        _compositeSave.setLayoutData(formData);

        _labelFileSaveCaption = new Label(_compositeSave, SWT.CENTER);
        final FormData formData_1 = new FormData();
        formData_1.bottom = new FormAttachment(100, 0);
        formData_1.right = new FormAttachment(30, 0);
        formData_1.top = new FormAttachment(0, 0);
        formData_1.left = new FormAttachment(0, 0);
        _labelFileSaveCaption.setLayoutData(formData_1);
        _labelFileSaveCaption.setFont(AGMStyledSWTForm.FONT_14);
        _labelFileSaveCaption.setBackground(AGMStyledSWTForm.COLOR_BLUE);
        _labelFileSaveCaption.setText("Название файла");

        _textFileName = new Text(_compositeSave, SWT.BORDER);
        _textFileName.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent arg0)
            {
                textFileNameOnKeyDown(arg0);
            }
        });
        _textFileName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0)
            {
                File file = new File(_directory + _textFileName.getText() + XmlUtils.PARAMS_FILE_EXTENSION);
                if(file.isFile())
                {
                    _labelMessage.setText(XmlUtils.MSG_FILE_EXISTS);
                    _labelSave.setText(CAPTION_REWRITE);
                }
                else
                {
                    _labelMessage.setText("");
                    _labelSave.setText(CAPTION_SAVE);
                }
            }
        });
        final FormData formData_2 = new FormData();
        formData_2.bottom = new FormAttachment(100, 0);
        formData_2.right = new FormAttachment(100, 0);
        formData_2.top = new FormAttachment(0, 0);
        formData_2.left = new FormAttachment(30, 0);
        _textFileName.setLayoutData(formData_2);
        _textFileName.setFont(AGMStyledSWTForm.FONT_14);

        final FormData formData6 = new FormData();
        formData6.top = new FormAttachment(100, -48);
        formData6.bottom = new FormAttachment(100, 0);
        formData6.right = new FormAttachment(100, 0);
        formData6.left = new FormAttachment(0, 0);
        _compositeBottomControls.setLayoutData(formData6);
        _compositeBottomControls.setLayout(new FillLayout());
        _compositeBottomControls.setBackground(AGMStyledSWTForm.COLOR_BLUE);

        _labelSave = new Label(_compositeBottomControls, SWT.CENTER | SWT.BORDER);
        _labelSave.setFont(AGMStyledSWTForm.FONT_12B);
        _labelSave.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        _labelSave.setText(CAPTION_SAVE);
        _labelSave.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
              String result = XmlUtils.writeParameters(_directory + _textFileName.getText() +
                  XmlUtils.PARAMS_FILE_EXTENSION, _inputParams, XML_ROOT);
              if (result == null)
                  close();
              else
                  _labelMessage.setText(result);
            }
        });
        
        
        _labelPreview = new Label(_compositeBottomControls, SWT.CENTER | SWT.BORDER);
        _labelPreview.setFont(AGMStyledSWTForm.FONT_12B);
        _labelPreview.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        _labelPreview.setText(CAPTION_VIEW);
        
        _labelPreview.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
              if (_listFileNames.getSelectionIndex() < 0)
                return;
              _labelMessage.setText("");
              _fileName = _directory + _listFileNames.getItem(_listFileNames.getSelectionIndex()) + XmlUtils.PARAMS_FILE_EXTENSION;
              _outputParams = new TreeMap();
              if ((_listFileNames.getItemCount() > 0) && (_listFileNames.getSelectionIndex() >= 0))
              {
                  String result = XmlUtils.readParameters(_fileName, _outputParams);
                  if (result == null)
                  {
                      loadParameters();
                      _outputParams = null;
                  }
                  else
                      _labelMessage.setText(result);
              }
            }
        });

        _labelDelete = new Label(_compositeBottomControls, SWT.CENTER | SWT.BORDER);
        _labelDelete.setFont(AGMStyledSWTForm.FONT_12B);
        _labelDelete.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        _labelDelete.setText(CAPTION_DELETE);

        _labelDelete.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
              removeParameters();   
            }
        });
        
        _labelLoad = new Label(_compositeBottomControls, SWT.CENTER | SWT.BORDER);
        _labelLoad.setFont(AGMStyledSWTForm.FONT_12B);
        _labelLoad.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        _labelLoad.setText(CAPTION_LOAD);
        
        _labelLoad.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
              if (_listFileNames.getSelectionIndex() < 0)
                return;
              _labelMessage.setText("");
              _fileName = _directory + _listFileNames.getItem(_listFileNames.getSelectionIndex()) + XmlUtils.PARAMS_FILE_EXTENSION;
              _outputParams = new TreeMap();
              if ((_listFileNames.getItemCount() > 0) && (_listFileNames.getSelectionIndex() >= 0))
              {
                  String result = XmlUtils.readParameters(_fileName, _outputParams);
                  if (result == null)
                      close();
                  else
                      _labelMessage.setText(result);
              }
            };
        });
    }

    protected void listOnKeyDown(KeyEvent event)
    {
        switch (event.keyCode)
        {
        case SWT.KEYPAD_CR:
        case 13:
            if (_listFileNames.getSelectionIndex() < 0)
                break;
            _labelMessage.setText("");
            _fileName = _directory + _listFileNames.getItem(_listFileNames.getSelectionIndex()) + XmlUtils.PARAMS_FILE_EXTENSION;
            _outputParams = new TreeMap();
            if (event.stateMask == SWT.CTRL)
            {
                if ((_listFileNames.getItemCount() > 0) && (_listFileNames.getSelectionIndex() >= 0))
                {
                    String result = XmlUtils.readParameters(_fileName, _outputParams);
                    if (result == null)
                        close();
                    else
                        _labelMessage.setText(result);
                }
            }
            else if (event.stateMask == 0)
                if ((_listFileNames.getItemCount() > 0) && (_listFileNames.getSelectionIndex() >= 0))
                {
                    String result = XmlUtils.readParameters(_fileName, _outputParams);
                    if (result == null)
                    {
                        loadParameters();
                        _outputParams = null;
                    }
                    else
                        _labelMessage.setText(result);
                }
        break;
        case SWT.DEL:
            removeParameters();
            break;
        case SWT.ARROW_UP:
        case SWT.ARROW_DOWN:
            if (_listFileNames.getItemCount() > 0)
                _labelMessage.setText("");
            break;
        }
    }

    protected void textFileNameOnKeyDown(KeyEvent event)
    {
        if (_textFileName.getText().trim().equals(""))
            return;
        if (event.keyCode == 's')
            if (event.stateMask == SWT.CTRL)
            {
                String result = XmlUtils.writeParameters(_directory + _textFileName.getText() +
                        XmlUtils.PARAMS_FILE_EXTENSION, _inputParams, XML_ROOT);
                if (result == null)
                    close();
                else
                    _labelMessage.setText(result);
            }
    }

    protected void close()
    {
        _shell.close();
    }

    protected void loadParameters()
    {
        if (_outputParams.size() <= 0)
        {
            _labelMessage.setText(XmlUtils.MSG_FILE_IS_EMPTY);
            return;
        }
        Object[] keys = _outputParams.keySet().toArray();
        String[] paramLines = new String[keys.length];
        for (int i = 0; i < keys.length; i++)
        {
            String name = (String)keys[i];
            String value = (String)_outputParams.get(keys[i]);
            paramLines[i] = name + " : " + value;
        }
        _listParams.setItems(paramLines);
        // FIXME bad solution! Why? I do not know!
        Point size = _compositeMain.computeSize(_compositeMain.getSize().x, _compositeMain.getSize().y);
        _compositeMain.setSize(size.x - 5, size.x - 5);
        _compositeMain.setSize(size.x - 4, size.y - 4);
    }

    protected void readFiles()
    {
        File file = new File(_directory);
        String[] fileNames = file.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                String files[] = StringUtils.split(name, ".");
                if (files.length == 2)
                    if (StringUtils.split(name, ".")[1].equals("xml"))
                        return true;
                return false;
            }
        });
        if (fileNames != null)
        {
            for (int i = 0; i < fileNames.length; i++)
                fileNames[i] = StringUtils.split(fileNames[i], ".")[0];
            _listFileNames.setItems(fileNames);
            if (_listFileNames.getItemCount() > 0)
                _listFileNames.select(0);
        }
    }

    protected void removeParameters()
    {
        String paramsFileName = _listFileNames.getItem(_listFileNames.getSelectionIndex());
        File file = new File(_directory + paramsFileName + XmlUtils.PARAMS_FILE_EXTENSION);
        if (file.exists())
            if (SWTFormBase.showQuetion("Удалить '" + paramsFileName + "'?") == SWT.YES)
                if (file.delete())
                    readFiles();
    }
}











