/*$Id: SaveMacrosParametersDialog.java,v 1.1 2010/05/13 05:50:55 Kulpanov Exp $*/
package ru.autogenmash.macros.cometmacros;

import java.io.File;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.autogenmash.gui.AGMStyledSWTForm;

/**
 * @author Dymarchuk Dmitry
 * @version
 * 08.05.2007 12:16:23
 */
public class SaveMacrosParametersDialog extends Dialog
{

    public static final String CAPTION_SAVE = "Сохранить\n(Enter)";

    protected Text _textXMLName;
    protected Label _labelMessage;
    protected Label _labelESC;
    protected Label _labelSave;

    protected String _directory;
    protected HashMap _params;

    protected Object _result = null;
    protected Shell _shell;


    public SaveMacrosParametersDialog(Shell parent, int style)
    {
        super(parent, style);
    }

    public SaveMacrosParametersDialog(Shell parent)
    {
        this(parent, SWT.NONE);
    }

    public Object open(String directory, HashMap params)
    {
        _directory = directory;
        _params = params;

        createContents();
        _shell.open();
        _shell.layout();
        Display display = getParent().getDisplay();
        while (!_shell.isDisposed())
        {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return _result;
    }

    protected void createContents()
    {
        int widht = 573;
        int height = 285;
        _shell = new Shell(getParent(), /*SWT.DIALOG_TRIM |*/ SWT.APPLICATION_MODAL);
        _shell.setLayout(new FormLayout());
        _shell.setSize(widht, height);
        _shell.setLocation(_shell.getParent().getBounds().width/2 - widht/2,
                _shell.getParent().getBounds().height/2 - height/2);

        final Composite composite = new Composite(_shell, SWT.BORDER);
        final FormData formData = new FormData();
        formData.bottom = new FormAttachment(0, 60);
        formData.top = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        formData.left = new FormAttachment(0, 0);
        composite.setLayoutData(formData);
        composite.setLayout(new FillLayout());

        final Label label = new Label(composite, SWT.CENTER);
        label.setFont(AGMStyledSWTForm.FONT_20B);
        label.setBackground(AGMStyledSWTForm.COLOR_BLUE);
        label.setText("Сохранение параметров макроса");

        final Composite composite_1 = new Composite(_shell, SWT.BORDER);
        final FormData formData_1 = new FormData();
        formData_1.top = new FormAttachment(composite, 0, SWT.BOTTOM);
        formData_1.right = new FormAttachment(100, 0);
        formData_1.left = new FormAttachment(0, 0);
        composite_1.setLayoutData(formData_1);
        composite_1.setLayout(new FormLayout());
        composite_1.setBackground(AGMStyledSWTForm.COLOR_BLUE);

        _textXMLName = new Text(composite_1, SWT.BORDER);
        _textXMLName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent arg0)
            {
                File file = new File(_directory + _textXMLName.getText() + ".xml");
                if(file.isFile())
                {
                    _labelMessage.setText("Файл с таким именем уже существует");
                    _labelSave.setText("Переписать\n(Enter)");
                }
                else
                {
                    _labelMessage.setText("");
                    _labelSave.setText(CAPTION_SAVE);
                }
            }
        });
        _textXMLName.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event)
            {
                switch (event.keyCode)
                {
                case 13:
                case SWT.KEYPAD_CR:
                    _result = new String(_textXMLName.getText());
                    _shell.close();
                    break;
//                case SWT.ESC:
//                    _result = new Boolean(false);
//                    break;
                }
            }
        });
        final FormData formData_2 = new FormData();
        formData_2.right = new FormAttachment(90, 0);
        formData_2.left = new FormAttachment(10, 0);
        formData_2.bottom = new FormAttachment(0, 85);
        formData_2.top = new FormAttachment(0, 40);
        _textXMLName.setLayoutData(formData_2);
        _textXMLName.setFont(AGMStyledSWTForm.FONT_20B);

        Composite composite_2;
        composite_2 = new Composite(_shell, SWT.BORDER);
        formData_1.bottom = new FormAttachment(composite_2, 0, SWT.TOP);

        _labelMessage = new Label(composite_1, SWT.NONE);
        _labelMessage.setForeground(AGMStyledSWTForm.COLOR_RED);
        _labelMessage.setFont(AGMStyledSWTForm.FONT_14);
        _labelMessage.setBackground(AGMStyledSWTForm.COLOR_BLUE);
        final FormData formData_4 = new FormData();
        formData_4.right = new FormAttachment(90, 0);
        formData_4.left = new FormAttachment(10, 0);
        formData_4.bottom = new FormAttachment(100, -5);
        formData_4.top = new FormAttachment(_textXMLName, 0, SWT.BOTTOM);
        _labelMessage.setLayoutData(formData_4);

        final FormData formData_3 = new FormData();
        formData_3.bottom = new FormAttachment(100, 0);
        formData_3.top = new FormAttachment(100, -65);
        formData_3.right = new FormAttachment(100, 0);
        formData_3.left = new FormAttachment(0, 0);
        composite_2.setLayoutData(formData_3);
        composite_2.setLayout(new FillLayout());
        composite_2.setBackground(AGMStyledSWTForm.COLOR_BLUE);

        _labelESC = new Label(composite_2, SWT.CENTER | SWT.BORDER);
        _labelESC.setFont(AGMStyledSWTForm.FONT_16B);
        _labelESC.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        _labelESC.setText("Отмена\n(Esc)");

        _labelSave = new Label(composite_2, SWT.CENTER | SWT.BORDER);
        _labelSave.setFont(AGMStyledSWTForm.FONT_16B);
        _labelSave.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        _labelSave.setText(CAPTION_SAVE);
        //
    }
}
