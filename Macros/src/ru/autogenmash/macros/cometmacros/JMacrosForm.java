/*$Id: JMacrosForm.java,v 1.5 2012/11/19 06:59:16 Kulpanov Exp $*/
package ru.autogenmash.macros.cometmacros;

import java.lang.reflect.Field;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import ru.autogenmash.gui.AGMStyledSWTForm;
import ru.autogenmash.gui.FilesViewDialog;
import ru.autogenmash.gui.StringInputDialog;


/**Класс для отображения макроса на экране.
 * @author Dymarchuk Dmitry
 * @version
 * 12.09.2007 9:20:44
 */
public class JMacrosForm
{
	public static final String MACRO_CPS_DIR = "macro_cps";

  private Combo comboDetailEndMovement;
	private Label labelDetailEndMovement;
	private Spinner textDistanceBeetwenDetails;
	private Label labelDistanceBeetwenDetails;
	private Spinner textDetailLeadInAngle;
	private Spinner textDetailLeadInLength;
	private Spinner textDetailLeadOutAngle;
	private Spinner textDetailLeadOutLength;
	private Spinner textDetailCurve;
	private Combo comboDetailCutOnType;
	private Label labelDetailPirsingPoint;
	private Label labelDetailDirection;
	private Label labelDetailCurve;
	private Label labelDetailLeadIn;
	private Label labelDetailLeadInLength;
	private Label labelDetailLeadInAngle;
	private Label labelDetailLeadOut;
	private Label labelDetailLeadOutLength;
	private Label labelDetailLeadOutAngle;
	private Group groupDetailLeadIn;
	private Group groupDetailLeadOut;
	private Combo comboDetailLeadIn;
	private Combo comboDetailLeadOut;
	private Combo comboDetailDirection;
	private Combo comboDetailPirsingPoint;
	private Spinner textHolesLeadInAngle;
	private Spinner textHolesLeadInLength;
	private Spinner textHolesLeadOutAngle;
	private Spinner textHolesLeadOutLength;
	private Spinner textHolesCurve;
	private Combo comboHolesCutOnType;
	private Label labelHolesDirection;
	private Label labelHolesCurve;
	private Label labelHolesLeadIn;
	private Label labelHolesLeadInLength;
	private Label labelHolesLeadInAngle;
	private Label labelHolesLeadOut;
	private Label labelHolesLeadOutLength;
	private Label labelHolesLeadOutAngle;
	private Group groupHolesLeadIn;
	private Group groupHolesLeadOut;
	private Combo comboHolesLeadIn;
	private Combo comboHolesLeadOut;
	private Combo comboHolesDirection;

	private Label labelCaption;

	private Composite compositeView;
	private Composite compositeDetailData;
	private Composite compositeHolesData;
	private Composite compositeGeometry;
	private ScrolledComposite scrolledCompositeGeometry;

	private TabFolder tabFolderControls;
	private TabItem tabItemDetailData;
	private TabItem tabItemHolesData;
	private TabItem tabItemGeometryData;

	private ProgressBar progressBar;

	public Label labelView;

	private JMacrosParameters	geoParameters;			// геометрические параметры макроса
	private int				macrosParametersCount;	// количество геометрических параметров макроса
	private JMacros				macros;					// весь макрос

	public Shell	shell;
	public Display	display;

	/** Динамические виджеты, будут созданы на основании geoParameters. */
	protected Label[] _labelGeoParameters;
	/** Динамические виджеты, будут созданы на основании geoParameters. */
	protected Spinner[] _textGeoParameters;

	private String	data;								// вспомогательные данные (применять по случаю)

  protected boolean _isActive = false;
  protected Listener _keyDownListener;
  protected Listener _focusInListener;
  protected Listener _focusOutListener;
  protected Composite compositeBottom;
  protected Label buttonSaveLoad;
  protected Label buttonOpenHelp;
  protected Label buttonOpenDetailTab;
  protected Label buttonOpenHoleTab;
  protected Label buttonOpenGeoTab;
  protected Label buttonGenerate;
  protected Composite compositeCaption;


	public JMacrosForm(JMacros _macros)
    {
		macros = _macros;
		macrosParametersCount = 0;

        _focusInListener = new Listener()
        {
            public void handleEvent(Event event)
            {
                OnGotFocus(event);
            }
        };

        _keyDownListener = new Listener()
        {
            public void handleEvent(Event event)
            {
                DisplayOnKeyDown(event);
            }
        };
        _focusOutListener = new Listener()
        {
			public void handleEvent(Event event)
            {
                OnLostFocus(event);
			}
		};
	}

	public void open() {
		// показать форму
		display = Display.getCurrent();

    display.addFilter(SWT.KeyDown, _keyDownListener);
    
		createContents();

        shell.addShellListener(new ShellAdapter()
        {
		    public void shellDeactivated(ShellEvent arg0)
		    {
		        _isActive = false;
		    }

		    public void shellActivated(ShellEvent arg0)
		    {
		        _isActive = true;
		    }
		});

		shell.open();
		shell.layout();

		macros.InitDrawParameters();

        // если на форме произошло получение фокуса у какого-либо виджета, обработать
		display.addFilter(SWT.FocusIn, _focusInListener);

		// если на форме произошла потеря фокуса у какого-либо виджета, обработать
		display.addFilter(SWT.FocusOut, _focusOutListener);

        // инициализировать размер шела
		shell.setBounds(0, 0, display.getBounds().width, display.getBounds().height);

		// создать на форме необходимые элементы управления для геометрических параметров
		CreateGeoParametersControls();

        // отрисовать макрос во внутренний image, а затем загрузить его в плот формы
		drawMacros();

        if (macros.GetShapeCount() == 1)
        {
            comboHolesLeadIn.select(0);
            comboHolesLeadIn.setEnabled(false);
            comboHolesLeadOut.setEnabled(false);
            textHolesLeadInAngle.setEnabled(false);
            textHolesLeadInLength.setEnabled(false);
            textHolesCurve.setEnabled(false);
            compositeHolesData.setEnabled(false);
        }

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	protected void createContents()
    {
		shell = new Shell(display, SWT.NULL);
    shell.setText("Макрос");
		shell.setLayout(new FormLayout());
		shell.setMaximized(true);

		compositeView = new Composite(shell, SWT.BORDER);
		compositeView.setLayout(new FormLayout());
		final FormData formData = new FormData();
		formData.bottom = new FormAttachment(100, -45);
		formData.top = new FormAttachment(0, 0);
		formData.right = new FormAttachment(100, -343);
		formData.left = new FormAttachment(0, 0);
		compositeView.setLayoutData(formData);
		compositeView.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		labelView = new Label(compositeView, SWT.CENTER);
		labelView.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		final FormData formData_4 = new FormData();
		formData_4.top = new FormAttachment(0, 0);
		formData_4.right = new FormAttachment(100, 0);
		formData_4.left = new FormAttachment(0, 0);
		labelView.setLayoutData(formData_4);

		progressBar = new ProgressBar(compositeView, SWT.NONE);
		formData_4.bottom = new FormAttachment(progressBar, 0, SWT.BOTTOM);
		progressBar.setVisible(false);
		progressBar.setSelection(33);
		final FormData formData_8 = new FormData();
		formData_8.bottom = new FormAttachment(100, 0);
		formData_8.top = new FormAttachment(100, -19);
		formData_8.right = new FormAttachment(100, 0);
		formData_8.left = new FormAttachment(0, 0);
		progressBar.setLayoutData(formData_8);

		tabFolderControls = new TabFolder(shell, SWT.NONE);
    tabFolderControls.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		tabFolderControls.setFont(AGMStyledSWTForm.FONT_14);
		final FormData formData_1 = new FormData();
		formData_1.bottom = new FormAttachment(100, -45 - 1);
		formData_1.top = new FormAttachment(0, 30);
		formData_1.right = new FormAttachment(100, 0);
		formData_1.left = new FormAttachment(100, -343);
		tabFolderControls.setLayoutData(formData_1);

		tabItemDetailData = new TabItem(tabFolderControls, SWT.NONE, 0);
		tabItemDetailData.setText("Деталь");
		tabItemHolesData = new TabItem(tabFolderControls, SWT.NONE, 1);
		tabItemHolesData.setText("Отверстия");
		tabItemGeometryData = new TabItem(tabFolderControls, SWT.NONE, 2);
		tabItemGeometryData.setText("Геометрия");

		tabFolderControls.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent event) {
				if(tabFolderControls.getSelectionIndex() == 0) comboDetailLeadIn.forceFocus();//comboShapeType.forceFocus();
				if(tabFolderControls.getSelectionIndex() == 1)
					if(macrosParametersCount > 0)
						_textGeoParameters[0].forceFocus();
			}
			public void widgetDefaultSelected(SelectionEvent event) {
			}
		});

		compositeHolesData = new Composite(tabFolderControls, SWT.NONE);
    compositeHolesData.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		compositeHolesData.setLayout(new FormLayout());
		tabItemHolesData.setControl(compositeHolesData);

		labelHolesDirection = new Label(compositeHolesData, SWT.NONE);
		labelHolesDirection.setVisible(false);
    labelHolesDirection.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelHolesDirection.setFont(AGMStyledSWTForm.FONT_14);
		labelHolesDirection.setText("Направление");
        final FormData formData_9_1_1_3_3 = new FormData();
        formData_9_1_1_3_3.right = new FormAttachment(0, 170);
        formData_9_1_1_3_3.left = new FormAttachment(0, 15);
        formData_9_1_1_3_3.bottom = new FormAttachment(0, 345);
        formData_9_1_1_3_3.top = new FormAttachment(0, 315);
        labelHolesDirection.setLayoutData(formData_9_1_1_3_3);

		(comboHolesDirection = new Combo(compositeHolesData, SWT.READ_ONLY)).setData("Отверстия:Направление");
		comboHolesDirection.setVisible(false);
		comboHolesDirection.setItems(new String[] {"прямое", "обратное"});
        final FormData formData_18_3 = new FormData();
        formData_18_3.right = new FormAttachment(100, -21);
        formData_18_3.left = new FormAttachment(0, 170);
        formData_18_3.bottom = new FormAttachment(0, 345);
        formData_18_3.top = new FormAttachment(0, 315);
        comboHolesDirection.setLayoutData(formData_18_3);
		comboHolesDirection.select(0);
		comboHolesDirection.setFont(AGMStyledSWTForm.FONT_10B);
		comboHolesDirection.setEnabled(false);

		labelHolesCurve = new Label(compositeHolesData, SWT.NONE);
    labelHolesCurve.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelHolesCurve.setFont(AGMStyledSWTForm.FONT_14);
		labelHolesCurve.setText("Эквидистанта");
        final FormData formData_9_1_1_2_1 = new FormData();
        formData_9_1_1_2_1.right = new FormAttachment(0, 170);
        formData_9_1_1_2_1.left = new FormAttachment(0, 15);
        formData_9_1_1_2_1.top = new FormAttachment(0, 280);
        formData_9_1_1_2_1.bottom = new FormAttachment(0, 310);
        labelHolesCurve.setLayoutData(formData_9_1_1_2_1);

		(textHolesCurve = new Spinner(compositeHolesData, SWT.BORDER)).setData("Отверстия:Эквидистанта");
        final FormData formData_14_1_1_1_1_1 = new FormData();
        formData_14_1_1_1_1_1.right = new FormAttachment(100, -21);
        formData_14_1_1_1_1_1.left = new FormAttachment(0, 170);
        formData_14_1_1_1_1_1.bottom = new FormAttachment(0, 310);
        formData_14_1_1_1_1_1.top = new FormAttachment(0, 280);
        textHolesCurve.setLayoutData(formData_14_1_1_1_1_1);
		textHolesCurve.setMaximum(1000);
		textHolesCurve.setFont(AGMStyledSWTForm.FONT_14);

        Label labelHolesCutOnType = new Label(compositeHolesData, 0);
        labelHolesCutOnType.setBackground(AGMStyledSWTForm.COLOR_BLUE);
        labelHolesCutOnType.setFont(AGMStyledSWTForm.FONT_14);
        labelHolesCutOnType.setText("Проб./врезка");
        final FormData formData_2 = new FormData();
        formData_2.right = new FormAttachment(0, 170);
        formData_2.left = new FormAttachment(0, 15);
        formData_2.top = new FormAttachment(0, 315);
        formData_2.bottom = new FormAttachment(0, 345);
        labelHolesCutOnType.setLayoutData(formData_2);

        (comboHolesCutOnType = new Combo(compositeHolesData, SWT.READ_ONLY)).setData("Отверстия:Проб./врезка");
        comboHolesCutOnType.setItems(new String[] {"пробивка", "врезка"});
        comboHolesCutOnType.select(0);
        final FormData formData_10 = new FormData();
        formData_10.right = new FormAttachment(100, -21);
        formData_10.left = new FormAttachment(0, 170);
        formData_10.top = new FormAttachment(0, 315);
        formData_10.bottom = new FormAttachment(0, 345);
        comboHolesCutOnType.setLayoutData(formData_10);
        comboHolesCutOnType.setFont(AGMStyledSWTForm.FONT_10B);

		groupHolesLeadIn = new Group(compositeHolesData, SWT.NONE);
    groupHolesLeadIn.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		groupHolesLeadIn.setLayout(new FormLayout());
		groupHolesLeadIn.setText("Подход");
		final FormData formData_5_2 = new FormData();
        formData_5_2.right = new FormAttachment(100, -16);
        formData_5_2.left = new FormAttachment(0, 15);
        formData_5_2.bottom = new FormAttachment(0, 135);
        formData_5_2.top = new FormAttachment(0, 5);
		groupHolesLeadIn.setLayoutData(formData_5_2);

		labelHolesLeadIn = new Label(groupHolesLeadIn, SWT.NONE);
    labelHolesLeadIn.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelHolesLeadIn.setFont(AGMStyledSWTForm.FONT_14);
		labelHolesLeadIn.setText("Подход");
		final FormData formData_9_1_1_4_2 = new FormData();
        formData_9_1_1_4_2.right = new FormAttachment(0, 150);
        formData_9_1_1_4_2.bottom = new FormAttachment(0, 35);
        formData_9_1_1_4_2.top = new FormAttachment(0, 5);
        formData_9_1_1_4_2.left = new FormAttachment(0, 5);
		labelHolesLeadIn.setLayoutData(formData_9_1_1_4_2);

		(comboHolesLeadIn = new Combo(groupHolesLeadIn, SWT.READ_ONLY)).setData("Отверстия:Подход");
		comboHolesLeadIn.setItems(new String[] {"нет", "касательная", "нормаль", "дуга"});
		comboHolesLeadIn.select(3);
		final FormData formData_14_1_1_2_2 = new FormData();
        formData_14_1_1_2_2.left = new FormAttachment(0, 155);
        formData_14_1_1_2_2.bottom = new FormAttachment(0, 35);
        formData_14_1_1_2_2.top = new FormAttachment(0, 5);
        formData_14_1_1_2_2.right = new FormAttachment(100, -5);
		comboHolesLeadIn.setLayoutData(formData_14_1_1_2_2);
		comboHolesLeadIn.setFont(AGMStyledSWTForm.FONT_10B);
		comboHolesLeadIn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				switch(comboHolesLeadIn.getSelectionIndex()){
				case 0:
					textHolesLeadInLength.setEnabled(false);
					textHolesLeadInAngle.setEnabled(false);
					break;
				case 3:
					textHolesLeadInLength.setEnabled(true);
					textHolesLeadInAngle.setEnabled(true);
					break;
				default:
					textHolesLeadInLength.setEnabled(true);
					textHolesLeadInAngle.setEnabled(false);
				}
			}
		});

		labelHolesLeadInLength = new Label(groupHolesLeadIn, SWT.NONE);
    labelHolesLeadInLength.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelHolesLeadInLength.setFont(AGMStyledSWTForm.FONT_14);
		labelHolesLeadInLength.setText("Длина");
		final FormData formData_9_1_1_3_1_2 = new FormData();
        formData_9_1_1_3_1_2.bottom = new FormAttachment(0, 70);
        formData_9_1_1_3_1_2.top = new FormAttachment(0, 40);
        formData_9_1_1_3_1_2.right = new FormAttachment(0, 150);
        formData_9_1_1_3_1_2.left = new FormAttachment(0, 5);
		labelHolesLeadInLength.setLayoutData(formData_9_1_1_3_1_2);

		(textHolesLeadInLength = new Spinner(groupHolesLeadIn, SWT.BORDER)).setData("Отверстия:Длина подхода");
		final FormData formData_18_1_2 = new FormData();
        formData_18_1_2.left = new FormAttachment(0, 155);
        formData_18_1_2.bottom = new FormAttachment(0, 70);
        formData_18_1_2.top = new FormAttachment(0, 40);
        formData_18_1_2.right = new FormAttachment(100, -5);
		textHolesLeadInLength.setLayoutData(formData_18_1_2);
		textHolesLeadInLength.setMaximum(10000);
		textHolesLeadInLength.setSelection(100);
		textHolesLeadInLength.setFont(AGMStyledSWTForm.FONT_14);

		labelHolesLeadInAngle = new Label(groupHolesLeadIn, SWT.NONE);
    labelHolesLeadInAngle.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelHolesLeadInAngle.setFont(AGMStyledSWTForm.FONT_14);
		labelHolesLeadInAngle.setText("Угол");
		final FormData formData_9_1_1_3_2_2 = new FormData();
        formData_9_1_1_3_2_2.bottom = new FormAttachment(0, 105);
        formData_9_1_1_3_2_2.top = new FormAttachment(0, 75);
        formData_9_1_1_3_2_2.right = new FormAttachment(0, 150);
        formData_9_1_1_3_2_2.left = new FormAttachment(0, 5);
		labelHolesLeadInAngle.setLayoutData(formData_9_1_1_3_2_2);

		(textHolesLeadInAngle = new Spinner(groupHolesLeadIn, SWT.BORDER)).setData("Отверстия:Угол подхода");
		final FormData formData_18_2_2 = new FormData();
        formData_18_2_2.left = new FormAttachment(0, 155);
        formData_18_2_2.bottom = new FormAttachment(0, 105);
        formData_18_2_2.top = new FormAttachment(0, 75);
        formData_18_2_2.right = new FormAttachment(100, -5);
		textHolesLeadInAngle.setLayoutData(formData_18_2_2);
		textHolesLeadInAngle.setMaximum(360);
		textHolesLeadInAngle.setSelection(150);
		textHolesLeadInAngle.setFont(AGMStyledSWTForm.FONT_14);

		groupHolesLeadOut = new Group(compositeHolesData, SWT.NONE);
        groupHolesLeadOut.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		final FormData formData_5_1_1 = new FormData();
        formData_5_1_1.right = new FormAttachment(100, -16);
        formData_5_1_1.bottom = new FormAttachment(0, 270);
        formData_5_1_1.top = new FormAttachment(0, 140);
        formData_5_1_1.left = new FormAttachment(0, 15);
		groupHolesLeadOut.setLayoutData(formData_5_1_1);
		groupHolesLeadOut.setLayout(new FormLayout());
		groupHolesLeadOut.setText("Отход");

		(comboHolesLeadOut = new Combo(groupHolesLeadOut, SWT.READ_ONLY)).setData("Отверстия:Отход");
		comboHolesLeadOut.setItems(new String[] {"нет", "касательная", "нормаль", "дуга"});
		final FormData formData_14_1_1_2_1_1 = new FormData();
        formData_14_1_1_2_1_1.left = new FormAttachment(0, 155);
        formData_14_1_1_2_1_1.bottom = new FormAttachment(0, 35);
        formData_14_1_1_2_1_1.top = new FormAttachment(0, 5);
        formData_14_1_1_2_1_1.right = new FormAttachment(100, -5);
		comboHolesLeadOut.setLayoutData(formData_14_1_1_2_1_1);
		comboHolesLeadOut.select(0);
		comboHolesLeadOut.setFont(AGMStyledSWTForm.FONT_10B);

		comboHolesLeadOut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				switch(comboHolesLeadOut.getSelectionIndex()){
				case 0:
					textHolesLeadOutLength.setEnabled(false);
					textHolesLeadOutAngle.setEnabled(false);
					break;
				case 3:
					textHolesLeadOutLength.setEnabled(true);
					textHolesLeadOutAngle.setEnabled(true);
					break;
				default:
					textHolesLeadOutLength.setEnabled(true);
					textHolesLeadOutAngle.setEnabled(false);
				}
			}
		});

		labelHolesLeadOut = new Label(groupHolesLeadOut, SWT.NONE);
    labelHolesLeadOut.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelHolesLeadOut.setFont(AGMStyledSWTForm.FONT_14);
		labelHolesLeadOut.setText("Отход");
		labelHolesLeadOut.setLayoutData(formData_9_1_1_4_2);

		labelHolesLeadOutLength = new Label(groupHolesLeadOut, SWT.NONE);
    labelHolesLeadOutLength.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelHolesLeadOutLength.setFont(AGMStyledSWTForm.FONT_14);
		labelHolesLeadOutLength.setText("Длина");
		final FormData formData_9_1_1_3_1_1_1 = new FormData();
        formData_9_1_1_3_1_1_1.bottom = new FormAttachment(0, 70);
        formData_9_1_1_3_1_1_1.top = new FormAttachment(0, 40);
        formData_9_1_1_3_1_1_1.right = new FormAttachment(0, 150);
        formData_9_1_1_3_1_1_1.left = new FormAttachment(0, 5);
		labelHolesLeadOutLength.setLayoutData(formData_9_1_1_3_1_1_1);

		(textHolesLeadOutLength = new Spinner(groupHolesLeadOut, SWT.BORDER)).setData("Отверстия:Длина отхода");
		textHolesLeadOutLength.setEnabled(false);
		final FormData formData_18_1_1_1 = new FormData();
        formData_18_1_1_1.left = new FormAttachment(0, 155);
        formData_18_1_1_1.bottom = new FormAttachment(0, 70);
        formData_18_1_1_1.top = new FormAttachment(0, 40);
        formData_18_1_1_1.right = new FormAttachment(100, -5);
		textHolesLeadOutLength.setLayoutData(formData_18_1_1_1);
		textHolesLeadOutLength.setMaximum(10000);
		textHolesLeadOutLength.setSelection(50);
		textHolesLeadOutLength.setFont(AGMStyledSWTForm.FONT_14);

		(textHolesLeadOutAngle = new Spinner(groupHolesLeadOut, SWT.BORDER)).setData("Отверстия:Угол отхода");
		textHolesLeadOutAngle.setEnabled(false);
		final FormData formData_18_2_1_1 = new FormData();
        formData_18_2_1_1.left = new FormAttachment(0, 155);
        formData_18_2_1_1.bottom = new FormAttachment(0, 105);
        formData_18_2_1_1.top = new FormAttachment(0, 75);
        formData_18_2_1_1.right = new FormAttachment(100, -5);
		textHolesLeadOutAngle.setLayoutData(formData_18_2_1_1);
		textHolesLeadOutAngle.setMaximum(360);
		textHolesLeadOutAngle.setSelection(45);
		textHolesLeadOutAngle.setFont(AGMStyledSWTForm.FONT_14);

		labelHolesLeadOutAngle = new Label(groupHolesLeadOut, SWT.NONE);
    labelHolesLeadOutAngle.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelHolesLeadOutAngle.setFont(AGMStyledSWTForm.FONT_14);
		labelHolesLeadOutAngle.setText("Угол");
		final FormData formData_9_1_1_3_2_1_1 = new FormData();
        formData_9_1_1_3_2_1_1.bottom = new FormAttachment(0, 105);
        formData_9_1_1_3_2_1_1.top = new FormAttachment(0, 75);
        formData_9_1_1_3_2_1_1.right = new FormAttachment(0, 150);
        formData_9_1_1_3_2_1_1.left = new FormAttachment(0, 5);
		labelHolesLeadOutAngle.setLayoutData(formData_9_1_1_3_2_1_1);
		compositeHolesData.setTabList(new Control[] {groupHolesLeadIn, groupHolesLeadOut, textHolesCurve, comboHolesCutOnType});

		scrolledCompositeGeometry = new ScrolledComposite(tabFolderControls, SWT.V_SCROLL);
		tabItemGeometryData.setControl(scrolledCompositeGeometry);

		compositeGeometry = new Composite(scrolledCompositeGeometry, SWT.NONE);
    compositeGeometry.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		compositeGeometry.setLayout(new FormLayout());
		scrolledCompositeGeometry.setContent(compositeGeometry);

		compositeDetailData = new Composite(tabFolderControls, SWT.NONE);
		compositeDetailData.setLayout(new FormLayout());
    compositeDetailData.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		tabItemDetailData.setControl(compositeDetailData);

		labelDetailCurve = new Label(compositeDetailData, SWT.NONE);
    labelDetailCurve.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDetailCurve.setFont(AGMStyledSWTForm.FONT_14);
		labelDetailCurve.setText("Эквидистанта");
		final FormData formData_9_1_1_3 = new FormData();
		formData_9_1_1_3.bottom = new FormAttachment(0, 415);
		formData_9_1_1_3.top = new FormAttachment(0, 385);
		formData_9_1_1_3.right = new FormAttachment(0, 170);
		formData_9_1_1_3.left = new FormAttachment(0, 15);
		labelDetailCurve.setLayoutData(formData_9_1_1_3);

		(textDetailCurve = new Spinner(compositeDetailData, SWT.BORDER)).setData("Деталь:Эквидистанта");
		textDetailCurve.setMaximum(1000);
		final FormData formData_18 = new FormData();
		formData_18.bottom = new FormAttachment(0, 415);
		formData_18.top = new FormAttachment(0, 385);
		formData_18.right = new FormAttachment(100, -21);
		formData_18.left = new FormAttachment(0, 170);
		textDetailCurve.setLayoutData(formData_18);
		textDetailCurve.setFont(AGMStyledSWTForm.FONT_14);

    Label labelDetailCutOnType = new Label(compositeDetailData, 0);
    labelDetailCutOnType.setBackground(AGMStyledSWTForm.COLOR_BLUE);
    labelDetailCutOnType.setFont(AGMStyledSWTForm.FONT_14);
    labelDetailCutOnType.setText("Проб./врезка");
    final FormData formData_11 = new FormData();
    formData_11.left = new FormAttachment(0, 15);
    formData_11.right = new FormAttachment(0, 170);
    formData_11.top = new FormAttachment(0, 420);
    formData_11.bottom = new FormAttachment(0, 450);
    labelDetailCutOnType.setLayoutData(formData_11);

    (comboDetailCutOnType = new Combo(compositeDetailData, SWT.READ_ONLY)).setData("Деталь:Проб./врезка");
    comboDetailCutOnType.setItems(new String[] {"пробивка", "врезка"});
    comboDetailCutOnType.select(0);
    final FormData formData_12 = new FormData();
    formData_12.right = new FormAttachment(100, -21);
    formData_12.left = new FormAttachment(0, 170);
    formData_12.top = new FormAttachment(0, 420);
    formData_12.bottom = new FormAttachment(0, 450);
    comboDetailCutOnType.setLayoutData(formData_12);
    comboDetailCutOnType.setFont(AGMStyledSWTForm.FONT_10B);

		labelDetailPirsingPoint = new Label(compositeDetailData, SWT.NONE);
    labelDetailPirsingPoint.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDetailPirsingPoint.setFont(AGMStyledSWTForm.FONT_14);
		labelDetailPirsingPoint.setText("Тчк. пробивки");
        final FormData formData_9_1_1_2 = new FormData();
        formData_9_1_1_2.bottom = new FormAttachment(0, 380);
        formData_9_1_1_2.top = new FormAttachment(0, 350);
        formData_9_1_1_2.right = new FormAttachment(0, 170);
        formData_9_1_1_2.left = new FormAttachment(0, 15);
        labelDetailPirsingPoint.setLayoutData(formData_9_1_1_2);

		(comboDetailPirsingPoint = new Combo(compositeDetailData, SWT.READ_ONLY)).setData("Деталь:Тчк.пробивки");
		comboDetailPirsingPoint.setFont(AGMStyledSWTForm.FONT_10B);
        final FormData formData_14_1_1_1_1 = new FormData();
        formData_14_1_1_1_1.bottom = new FormAttachment(0, 380);
        formData_14_1_1_1_1.top = new FormAttachment(0, 350);
        formData_14_1_1_1_1.right = new FormAttachment(100, -21);
        formData_14_1_1_1_1.left = new FormAttachment(0, 170);
    comboDetailPirsingPoint.setLayoutData(formData_14_1_1_1_1);
		comboDetailPirsingPoint.select(0);

        labelDetailDirection = new Label(compositeDetailData, SWT.NONE);
        labelDetailDirection.setVisible(false);
        labelDetailDirection.setBackground(AGMStyledSWTForm.COLOR_BLUE);
        labelDetailDirection.setFont(AGMStyledSWTForm.FONT_14);
        labelDetailDirection.setText("Направление");
        final FormData formData_9 = new FormData();
        formData_9.bottom = new FormAttachment(0, 450);
        formData_9.top = new FormAttachment(0, 420);
        formData_9.right = new FormAttachment(0, 170);
        formData_9.left = new FormAttachment(0, 15);
        labelDetailDirection.setLayoutData(formData_9);

        (comboDetailDirection = new Combo(compositeDetailData, SWT.READ_ONLY)).setData("Деталь:Направление");
        comboDetailDirection.setVisible(false);
        comboDetailDirection.setFont(AGMStyledSWTForm.FONT_10B);
        comboDetailDirection.setEnabled(false);
        comboDetailDirection.setItems(new String[] {"прямое", "обратное"});
        comboDetailDirection.select(0);
        final FormData formData_14 = new FormData();
        formData_14.bottom = new FormAttachment(0, 450);
        formData_14.top = new FormAttachment(0, 420);
        formData_14.right = new FormAttachment(100, -21);
        formData_14.left = new FormAttachment(0, 170);
        comboDetailDirection.setLayoutData(formData_14);

		groupDetailLeadIn = new Group(compositeDetailData, SWT.NONE);
		groupDetailLeadIn.setText("Подход");
    groupDetailLeadIn.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		final FormData formData_5 = new FormData();
		formData_5.right = new FormAttachment(100, -16);
		formData_5.left = new FormAttachment(0, 15);
		formData_5.bottom = new FormAttachment(0, 135);
		formData_5.top = new FormAttachment(0, 5);
		groupDetailLeadIn.setLayoutData(formData_5);
		groupDetailLeadIn.setLayout(new FormLayout());

		labelDetailLeadIn = new Label(groupDetailLeadIn, SWT.NONE);
        labelDetailLeadIn.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDetailLeadIn.setFont(AGMStyledSWTForm.FONT_14);
		labelDetailLeadIn.setText("Подход");
		final FormData formData_9_1_1_4 = new FormData();
		formData_9_1_1_4.right = new FormAttachment(0, 150);
		formData_9_1_1_4.bottom = new FormAttachment(0, 35);
		formData_9_1_1_4.top = new FormAttachment(0, 5);
		formData_9_1_1_4.left = new FormAttachment(0, 5);
		labelDetailLeadIn.setLayoutData(formData_9_1_1_4);

		(comboDetailLeadIn = new Combo(groupDetailLeadIn, SWT.READ_ONLY)).setData("Деталь:Подход");
		comboDetailLeadIn.setFont(AGMStyledSWTForm.FONT_10B);
		comboDetailLeadIn.setItems(new String[] {"нет", "касательная", "нормаль", "дуга"});
		comboDetailLeadIn.select(0);

		comboDetailLeadIn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				switch(comboDetailLeadIn.getSelectionIndex()){
				case 0:
					textDetailLeadInLength.setEnabled(false);
					textDetailLeadInAngle.setEnabled(false);
					break;
				case 3:
					textDetailLeadInLength.setEnabled(true);
					textDetailLeadInAngle.setEnabled(true);
					break;
				default:
					textDetailLeadInLength.setEnabled(true);
					textDetailLeadInAngle.setEnabled(false);
				}
			}
		});
		final FormData formData_14_1_1_2 = new FormData();
		formData_14_1_1_2.left = new FormAttachment(0, 155);
		formData_14_1_1_2.bottom = new FormAttachment(0, 35);
		formData_14_1_1_2.top = new FormAttachment(0, 5);
		formData_14_1_1_2.right = new FormAttachment(100, -5);
		comboDetailLeadIn.setLayoutData(formData_14_1_1_2);

		labelDetailLeadInLength = new Label(groupDetailLeadIn, SWT.NONE);
		labelDetailLeadInLength.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDetailLeadInLength.setFont(AGMStyledSWTForm.FONT_14);
		labelDetailLeadInLength.setText("Длина");
		final FormData formData_9_1_1_3_1 = new FormData();
		formData_9_1_1_3_1.bottom = new FormAttachment(0, 70);
		formData_9_1_1_3_1.top = new FormAttachment(0, 40);
		formData_9_1_1_3_1.right = new FormAttachment(0, 150);
		formData_9_1_1_3_1.left = new FormAttachment(0, 5);
		labelDetailLeadInLength.setLayoutData(formData_9_1_1_3_1);

		(textDetailLeadInLength = new Spinner(groupDetailLeadIn, SWT.BORDER)).setData("Деталь:Длина подхода");
		textDetailLeadInLength.setMaximum(10000);
		textDetailLeadInLength.setSelection(100);
		final FormData formData_18_1 = new FormData();
		formData_18_1.left = new FormAttachment(0, 155);
		formData_18_1.bottom = new FormAttachment(0, 70);
		formData_18_1.top = new FormAttachment(0, 40);
		formData_18_1.right = new FormAttachment(100, -5);
		textDetailLeadInLength.setLayoutData(formData_18_1);
		textDetailLeadInLength.setFont(AGMStyledSWTForm.FONT_14);

		labelDetailLeadInAngle = new Label(groupDetailLeadIn, SWT.NONE);
        labelDetailLeadInAngle.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDetailLeadInAngle.setFont(AGMStyledSWTForm.FONT_14);
		labelDetailLeadInAngle.setText("Угол");
		final FormData formData_9_1_1_3_2 = new FormData();
		formData_9_1_1_3_2.bottom = new FormAttachment(0, 105);
		formData_9_1_1_3_2.top = new FormAttachment(0, 75);
		formData_9_1_1_3_2.right = new FormAttachment(0, 150);
		formData_9_1_1_3_2.left = new FormAttachment(0, 5);
		labelDetailLeadInAngle.setLayoutData(formData_9_1_1_3_2);

		(textDetailLeadInAngle = new Spinner(groupDetailLeadIn, SWT.BORDER)).setData("Деталь:Угол подхода");
		textDetailLeadInAngle.setEnabled(false);
		textDetailLeadInAngle.setMaximum(360);
		textDetailLeadInAngle.setSelection(90);
		final FormData formData_18_2 = new FormData();
		formData_18_2.left = new FormAttachment(0, 155);
		formData_18_2.bottom = new FormAttachment(0, 105);
		formData_18_2.top = new FormAttachment(0, 75);
		formData_18_2.right = new FormAttachment(100, -5);
		textDetailLeadInAngle.setLayoutData(formData_18_2);
		textDetailLeadInAngle.setFont(AGMStyledSWTForm.FONT_14);
		groupDetailLeadIn.setTabList(new Control[] {comboDetailLeadIn, textDetailLeadInLength, textDetailLeadInAngle});

		groupDetailLeadOut = new Group(compositeDetailData, SWT.NONE);
        groupDetailLeadOut.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		groupDetailLeadOut.setText("Отход");
		final FormData formData_5_1 = new FormData();
		formData_5_1.right = new FormAttachment(100, -16);
		formData_5_1.bottom = new FormAttachment(0, 270);
		formData_5_1.top = new FormAttachment(0, 140);
		formData_5_1.left = new FormAttachment(0, 15);
		groupDetailLeadOut.setLayoutData(formData_5_1);
		groupDetailLeadOut.setLayout(new FormLayout());

		(comboDetailLeadOut = new Combo(groupDetailLeadOut, SWT.READ_ONLY)).setData("Деталь:Отход");
		comboDetailLeadOut.setFont(AGMStyledSWTForm.FONT_10B);
		comboDetailLeadOut.setItems(new String[] {"нет", "касательная", "нормаль", "дуга"});
		comboDetailLeadOut.select(0);

		comboDetailLeadOut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent arg0) {
				switch(comboDetailLeadOut.getSelectionIndex()){
				case 0:
					textDetailLeadOutLength.setEnabled(false);
					textDetailLeadOutAngle.setEnabled(false);
					break;
				case 3:
					textDetailLeadOutLength.setEnabled(true);
					textDetailLeadOutAngle.setEnabled(true);
					break;
				default:
					textDetailLeadOutLength.setEnabled(true);
					textDetailLeadOutAngle.setEnabled(false);
				}
			}
		});
		final FormData formData_14_1_1_2_1 = new FormData();
		formData_14_1_1_2_1.left = new FormAttachment(0, 155);
		formData_14_1_1_2_1.bottom = new FormAttachment(0, 35);
		formData_14_1_1_2_1.top = new FormAttachment(0, 5);
		formData_14_1_1_2_1.right = new FormAttachment(100, -5);
		comboDetailLeadOut.setLayoutData(formData_14_1_1_2_1);

		labelDetailLeadOut = new Label(groupDetailLeadOut, SWT.NONE);
        labelDetailLeadOut.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDetailLeadOut.setFont(AGMStyledSWTForm.FONT_14);
		labelDetailLeadOut.setText("Отход");
		final FormData formData_9_1_1_4_1 = new FormData();
		formData_9_1_1_4_1.right = new FormAttachment(0, 150);
		formData_9_1_1_4_1.bottom = new FormAttachment(0, 35);
		formData_9_1_1_4_1.top = new FormAttachment(0, 5);
		formData_9_1_1_4_1.left = new FormAttachment(0, 5);
		labelDetailLeadOut.setLayoutData(formData_9_1_1_4_1);

		labelDetailLeadOutLength = new Label(groupDetailLeadOut, SWT.NONE);
        labelDetailLeadOutLength.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDetailLeadOutLength.setFont(AGMStyledSWTForm.FONT_14);
		labelDetailLeadOutLength.setText("Длина");
		final FormData formData_9_1_1_3_1_1 = new FormData();
		formData_9_1_1_3_1_1.bottom = new FormAttachment(0, 70);
		formData_9_1_1_3_1_1.top = new FormAttachment(0, 40);
		formData_9_1_1_3_1_1.right = new FormAttachment(0, 150);
		formData_9_1_1_3_1_1.left = new FormAttachment(0, 5);
		labelDetailLeadOutLength.setLayoutData(formData_9_1_1_3_1_1);

		(textDetailLeadOutLength = new Spinner(groupDetailLeadOut, SWT.BORDER)).setData("Деталь:Длина отхода");
		textDetailLeadOutLength.setMaximum(10000);
		textDetailLeadOutLength.setSelection(100);
		final FormData formData_18_1_1 = new FormData();
		formData_18_1_1.left = new FormAttachment(0, 155);
		formData_18_1_1.bottom = new FormAttachment(0, 70);
		formData_18_1_1.top = new FormAttachment(0, 40);
		formData_18_1_1.right = new FormAttachment(100, -5);
		textDetailLeadOutLength.setLayoutData(formData_18_1_1);
		textDetailLeadOutLength.setFont(AGMStyledSWTForm.FONT_14);

		(textDetailLeadOutAngle = new Spinner(groupDetailLeadOut, SWT.BORDER)).setData("Деталь:Угол отхода");
		textDetailLeadOutAngle.setEnabled(false);
		textDetailLeadOutAngle.setMaximum(360);
		textDetailLeadOutAngle.setSelection(90);
		final FormData formData_18_2_1 = new FormData();
		formData_18_2_1.left = new FormAttachment(0, 155);
		formData_18_2_1.bottom = new FormAttachment(0, 105);
		formData_18_2_1.top = new FormAttachment(0, 75);
		formData_18_2_1.right = new FormAttachment(100, -5);
		textDetailLeadOutAngle.setLayoutData(formData_18_2_1);
		textDetailLeadOutAngle.setFont(AGMStyledSWTForm.FONT_14);

		labelDetailLeadOutAngle = new Label(groupDetailLeadOut, SWT.NONE);
        labelDetailLeadOutAngle.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDetailLeadOutAngle.setFont(AGMStyledSWTForm.FONT_14);
		labelDetailLeadOutAngle.setText("Угол");
		final FormData formData_9_1_1_3_2_1 = new FormData();
		formData_9_1_1_3_2_1.bottom = new FormAttachment(0, 105);
		formData_9_1_1_3_2_1.top = new FormAttachment(0, 75);
		formData_9_1_1_3_2_1.right = new FormAttachment(0, 150);
		formData_9_1_1_3_2_1.left = new FormAttachment(0, 5);
		labelDetailLeadOutAngle.setLayoutData(formData_9_1_1_3_2_1);

		labelDistanceBeetwenDetails = new Label(compositeDetailData, SWT.NONE);
		final FormData formData_9_1_1_3_4 = new FormData();
		formData_9_1_1_3_4.right = new FormAttachment(0, 170);
		formData_9_1_1_3_4.left = new FormAttachment(0, 15);
		formData_9_1_1_3_4.top = new FormAttachment(0, 280);
		formData_9_1_1_3_4.bottom = new FormAttachment(0, 310);
		labelDistanceBeetwenDetails.setLayoutData(formData_9_1_1_3_4);
		labelDistanceBeetwenDetails.setFont(AGMStyledSWTForm.FONT_14);
		labelDistanceBeetwenDetails.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDistanceBeetwenDetails.setText(JMacros.DISTANCE_BETWEEN_SHAPES);

		(textDistanceBeetwenDetails = new Spinner(compositeDetailData, SWT.BORDER)).setData("Деталь:Переход");
		final FormData formData_18_4 = new FormData();
		formData_18_4.right = new FormAttachment(100, -21);
		formData_18_4.left = new FormAttachment(0, 170);
		formData_18_4.bottom = new FormAttachment(0, 310);
		formData_18_4.top = new FormAttachment(0, 280);
		textDistanceBeetwenDetails.setLayoutData(formData_18_4);
		textDistanceBeetwenDetails.setMaximum(1000);
		textDistanceBeetwenDetails.setFont(AGMStyledSWTForm.FONT_14);
        textDistanceBeetwenDetails.setSelection(macros.getDistanceBetweenShapes());

		labelDetailEndMovement = new Label(compositeDetailData, SWT.NONE);
		final FormData formData_9_1_1_2_2 = new FormData();
		formData_9_1_1_2_2.right = new FormAttachment(0, 170);
		formData_9_1_1_2_2.left = new FormAttachment(0, 15);
		formData_9_1_1_2_2.bottom = new FormAttachment(0, 345);
		formData_9_1_1_2_2.top = new FormAttachment(0, 315);
		labelDetailEndMovement.setLayoutData(formData_9_1_1_2_2);
		labelDetailEndMovement.setFont(AGMStyledSWTForm.FONT_14);
		labelDetailEndMovement.setBackground(AGMStyledSWTForm.COLOR_BLUE);
		labelDetailEndMovement.setText("Выход");

		(comboDetailEndMovement = new Combo(compositeDetailData, SWT.READ_ONLY)).setData("Деталь:Выход");
		comboDetailEndMovement.setItems(new String[] {"Нет", "В начало", "X", "Y"});
		final FormData formData_14_1_1_1_1_2 = new FormData();
		formData_14_1_1_1_1_2.right = new FormAttachment(100, -21);
		formData_14_1_1_1_1_2.left = new FormAttachment(0, 170);
		formData_14_1_1_1_1_2.bottom = new FormAttachment(0, 345);
		formData_14_1_1_1_1_2.top = new FormAttachment(0, 315);
		comboDetailEndMovement.setLayoutData(formData_14_1_1_1_1_2);
		comboDetailEndMovement.select(0);
		comboDetailEndMovement.setFont(AGMStyledSWTForm.FONT_10B);

		groupDetailLeadOut.setTabList(new Control[] {comboDetailLeadOut, textDetailLeadOutLength, textDetailLeadOutAngle});
		compositeDetailData.setTabList(new Control[] {groupDetailLeadIn, groupDetailLeadOut, textDistanceBeetwenDetails,
                comboDetailEndMovement, comboDetailPirsingPoint, textDetailCurve, comboDetailCutOnType});

		compositeBottom = new Composite(shell, SWT.BORDER);
		compositeBottom.setLayout(new FillLayout());
		final FormData formData_6 = new FormData();
		formData_6.left = new FormAttachment(0, 0);
		formData_6.top = new FormAttachment(100, -45);
		formData_6.bottom = new FormAttachment(100, 2);
		formData_6.right = new FormAttachment(100, 0);
		compositeBottom.setLayoutData(formData_6);

        buttonOpenHelp = new Label(compositeBottom, SWT.CENTER | SWT.BORDER);
        buttonOpenHelp.setFont(AGMStyledSWTForm.FONT_12B);
        buttonOpenHelp.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        buttonOpenHelp.setText("Справка\n(F1)");
        buttonOpenHelp.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
                onF1Pressed();
            }
        });

        buttonOpenDetailTab = new Label(compositeBottom, SWT.CENTER | SWT.BORDER);
        buttonOpenDetailTab.setFont(AGMStyledSWTForm.FONT_12B);
        buttonOpenDetailTab.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        buttonOpenDetailTab.setText("Деталь\n(F5)");
        buttonOpenDetailTab.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
                onF5Pressed();
            }
        });

        buttonOpenHoleTab = new Label(compositeBottom, SWT.CENTER | SWT.BORDER);
        buttonOpenHoleTab.setFont(AGMStyledSWTForm.FONT_12B);
        buttonOpenHoleTab.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        buttonOpenHoleTab.setText("Отверстия\n(F6)");
        buttonOpenHoleTab.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
                onF6Pressed();
            }
        });

        buttonOpenGeoTab = new Label(compositeBottom, SWT.CENTER | SWT.BORDER);
        buttonOpenGeoTab.setFont(AGMStyledSWTForm.FONT_12B);
        buttonOpenGeoTab.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        buttonOpenGeoTab.setText("Геометрия\n(F7)");
        buttonOpenGeoTab.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
                onF7Pressed();
            }
        });

        buttonSaveLoad = new Label(compositeBottom, SWT.CENTER | SWT.BORDER);
        buttonSaveLoad.setFont(AGMStyledSWTForm.FONT_12B);
        buttonSaveLoad.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        buttonSaveLoad.setText("Файл\n(F8)");
        buttonSaveLoad.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
                onF8Pressed();
            }
        });

        buttonGenerate = new Label(compositeBottom, SWT.CENTER | SWT.BORDER);
        buttonGenerate.setFont(AGMStyledSWTForm.FONT_12B);
        buttonGenerate.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
        buttonGenerate.setText("Создать УП\n(F10)");
        buttonGenerate.addMouseListener(new MouseAdapter()
        {
            public void mouseDown(MouseEvent e)
            {
                generateCP();
            }
        });

		compositeCaption = new Composite(shell, SWT.NONE);
		compositeCaption.setLayout(new FillLayout());
		final FormData formData_3 = new FormData();
		formData_3.bottom = new FormAttachment(0, 30);
		formData_3.top = new FormAttachment(0, 0);
		formData_3.right = new FormAttachment(100, 0);
		formData_3.left = new FormAttachment(100, -343);
		compositeCaption.setLayoutData(formData_3);

		labelCaption = new Label(compositeCaption, SWT.CENTER);
        labelCaption.setBackground(AGMStyledSWTForm.COLOR_DARK_BLUE);
		labelCaption.setFont(AGMStyledSWTForm.FONT_16);

		labelCaption.setText(macros.macrosName);
	}


/****************************************************************************************************************/
/*******************************		       Common functions				*************************************/
/****************************************************************************************************************/


	protected void CreateGeoParametersControls()
    {
		// создать элементы управления на форме для контроля над геометрическими параметрами макроса
		geoParameters = new JMacrosParameters();
		geoParameters = macros.GetGeoParameters();

		macrosParametersCount = geoParameters.GetLength();

		_labelGeoParameters	= new Label[macrosParametersCount];
		_textGeoParameters	= new Spinner[macrosParametersCount];

		Point labelSize	= new Point(140, 30);
		Point textSize	= new Point(150, 30);
		Point distanceBetweenControls = new Point(10, 10);
		int compositeHeight = ( Math.max(labelSize.y, textSize.y) + 10) * macrosParametersCount + 10;

		compositeGeometry.setSize(compositeDetailData.getBounds().width, Math.max(compositeDetailData.getBounds().height, compositeHeight));

		for (int i = 0; i < macrosParametersCount; i++)
        {
			_labelGeoParameters[i] = new Label(compositeGeometry, SWT.CENTER);
      _labelGeoParameters[i].setBackground(AGMStyledSWTForm.COLOR_BLUE);
      _labelGeoParameters[i].setFont(AGMStyledSWTForm.FONT_14);
			//_labelGeoParameters[i].setText(geoParameters.GetName(i) + " (мм)");
			_labelGeoParameters[i].setText(geoParameters.GetName(i));
			_labelGeoParameters[i].setBounds(distanceBetweenControls.x, distanceBetweenControls.y + (distanceBetweenControls.y + Math.max(labelSize.y, textSize.y))*i, labelSize.x, labelSize.y);

			_textGeoParameters[i] = new Spinner(compositeGeometry, SWT.BORDER);
        _textGeoParameters[i].setData(geoParameters.GetName(i));
			_textGeoParameters[i].setMaximum(300000);
			_textGeoParameters[i].setSelection(geoParameters.GetValue(i));
			_textGeoParameters[i].setFont(AGMStyledSWTForm.FONT_14);
			_textGeoParameters[i].setBounds(labelSize.x + 2*distanceBetweenControls.x, distanceBetweenControls.y + (distanceBetweenControls.y + Math.max(labelSize.y, textSize.y))*i, textSize.x, textSize.y);
		}

		// заполнить точки пробивки в comboBox на форме
		JCPList CPListSource;
		for(int i = 0; i < macros.shapeList.size(); i++) {
			CPListSource = ((JCPList)macros.shapeList.get(i));
			for(int j = 0; j < CPListSource.getLength(); j++) {
			if( CPListSource.getCCType(j) == JCC.StartPoint )
				if( CPListSource.getType() == JCPList.SHAPE )
					comboDetailPirsingPoint.add(String.valueOf(CPListSource.getData(j)[0]));
				//else
					//comboHolesPirsingPoint.add(String.valueOf(CPListSource.GetData(j)[0]));
			}
		}
		comboDetailPirsingPoint.select(0);
	}

    public int getDistanceBetweenDetails()
    {
        return textDistanceBeetwenDetails.getSelection();
    }

    public int getExitType()
    {
        return comboDetailEndMovement.getSelectionIndex();
    }

	private void FillGeoParameters() {
		// заполнить параметры макроса на основании данных с формы
		// функция различная для каждого макроса

		geoParameters = new JMacrosParameters();

		for(int i = 0; i < macrosParametersCount; i++) {
			// добавить параметр
			geoParameters.AddParameter(_labelGeoParameters[i].getText(), _textGeoParameters[i].getSelection());
		}

		// загрузить новые параметры на отрисовку
		macros.LoadParameters(geoParameters);

		macros.SetLeadInLeadOutParams(textDetailLeadInLength.getSelection(), textDetailLeadOutLength.getSelection(), textHolesLeadInLength.getSelection(), textHolesLeadOutLength.getSelection(),
									  textDetailLeadInAngle.getSelection(),  textDetailLeadOutAngle.getSelection(),  textHolesLeadInAngle.getSelection(),  textHolesLeadOutAngle.getSelection(),
									  comboDetailLeadIn.getSelectionIndex(), comboDetailLeadOut.getSelectionIndex(), comboHolesLeadIn.getSelectionIndex(), comboHolesLeadOut.getSelectionIndex(),
									  Integer.parseInt(comboDetailPirsingPoint.getText()), 1);
    macros.setDetailKerf(textDetailCurve.getSelection());
    macros.setHolesKerf(textHolesCurve.getSelection());

    macros.setDetailCutOnCommand(71 + comboDetailCutOnType.getSelectionIndex());
    macros.setHolesCutOnCommand(71 + comboHolesCutOnType.getSelectionIndex());
	}

	public void LoadImage(Image _image) {
		// загрузить изображение
		labelView.setImage(_image);
	}

	public void drawMacros()
    {
	    if (checkAndInit())
            macros.DrawMacros();
	}

	/**Сгенерировать управляющую программу.
	 *
	 */
	public void generateCP()
    {
        if (checkAndInit())
        {
            StringInputDialog dialog = new StringInputDialog(shell, SWT.NULL);
            dialog.setText(macros.CPName);
            Object result = dialog.open("Название УП", "Применить");
            if (result == null)
                return;
            String cpName = (String)result;
            if (cpName.length() <= 0)
                cpName = null;

            macros.GenerateCP(cpName);
        }
	}

    protected boolean checkAndInit()
    {
        // заполнить геометрические параметры данными с формы и передать их в вызывающий макрос
        FillGeoParameters();

        // проверка на правильность заполнения размеров детали
        if (macros.CheckForCorrectSize() == false)
        {
            MessageBox mbError = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK );
            mbError.setMessage("Некорректно заданы размеры объекта");
            mbError.setText("Ошибка");
            mbError.open();
            return false;
        }

        // создать прообраз макроса
        macros.FillShapeList();
        // заполнить список линейных размеров для отображения в плоте
        macros.FillLineSizeList();
        // на основании заданных контуров сформировать траектории
        macros.FillTrajectoryList();
        // отрисовать макрос во внутренний image, а затем загрузить его в плот формы

        if (System.getProperty("os.name").equals("QNX") == false)
            if (macros.checkMacrosGeometry() == false)
            {
                MessageBox mbError = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK );
                mbError.setMessage("Некорректно заданы размеры объекта");
                mbError.setText("Ошибка");
                mbError.open();
                return false;
            }
        return true;
    }


/****************************************************************************************************************/
/*******************************				Event handlers				*************************************/
/****************************************************************************************************************/


	protected void OnGotFocus(Event event)
    {
		//
		if (event.widget instanceof Spinner)
			data = String.valueOf(((Spinner)event.widget).getSelection());
		if (event.widget instanceof Combo)
			data = String.valueOf(((Combo)event.widget).getText());
		if (event.widget instanceof Text)
			data = String.valueOf(((Text)event.widget).getText());
	}

	/**Обработка потери фокуса где-либо на форме.
	 * @param event
	 */
	protected void OnLostFocus(Event event)
    {
        boolean redraw = false;

		if (event.widget instanceof Spinner)
			if (data.equalsIgnoreCase(String.valueOf(((Spinner)event.widget).getSelection())))
				return;
			else
				redraw = true;
		if (event.widget instanceof Combo)
			if (data.equalsIgnoreCase(String.valueOf(((Combo)event.widget).getText())))
				return;
			else
				redraw = true;
		if (event.widget instanceof Text)
			if (data.equalsIgnoreCase(String.valueOf(((Text)event.widget).getText())))
				return;
			else
				redraw = true;

		if (redraw)
			drawMacros();
	}

	protected void DisplayOnKeyDown(Event event)
    {
        if (_isActive == false)
            return;

		switch (event.keyCode)
        {
		case SWT.ESC:
            display.removeFilter(SWT.KeyDown, _keyDownListener);
            display.removeFilter(SWT.FocusIn, _focusInListener);
            display.removeFilter(SWT.FocusOut, _focusOutListener);
            shell.close();
			break;
        case 13:
        case SWT.KEYPAD_CR:
            OnLostFocus(event);
            OnGotFocus(event);
            break;
		case SWT.F10:
			generateCP();
			break;
		case SWT.F5:
			if (event.stateMask == 0)
				onF5Pressed();
			break;
		case SWT.F6:
			if (event.stateMask == 0)
                onF6Pressed();
			break;
		case SWT.F7:
			if (event.stateMask == 0)
				onF7Pressed();
			break;
        case SWT.F8:
            if (event.stateMask == 0)
                onF8Pressed();
            break;
        case SWT.F1:
            if (event.stateMask == 0)
                onF1Pressed();
            break;
		}
	}

	private void onF1Pressed()
	{
	    TreeMap helpFiles = new TreeMap();
	    helpFiles.put("1.Общая информация", "./help.txt");
	    helpFiles.put("2.Быстрый старт", "./quick_start.txt");
	    FilesViewDialog help = new FilesViewDialog(shell, helpFiles);
	    help.open("Справка");
	}

    private void onF5Pressed()
    {
        tabFolderControls.setSelection(0);
        SetFocus();
    }

    private void onF6Pressed()
    {
        if(macros.GetShapeCount() == 1)
            return;
        tabFolderControls.setSelection(1);
        SetFocus();
    }

    private void onF7Pressed()
    {
        tabFolderControls.setSelection(2);
        SetFocus();
    }

    private void onF8Pressed()
    {
        try
        {
            MacrosParametersDialog dialog = new MacrosParametersDialog(shell);
            TreeMap inputParams = getParams();
            TreeMap outputParams = dialog.open(macros.macrosName, MACRO_CPS_DIR + "/" + macros.CPName + "/",
                    inputParams);
            if (outputParams != null)
            {
                setParams(outputParams);
                drawMacros();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void setParams(TreeMap outputParams) throws IllegalArgumentException, IllegalAccessException
    {
        Field[] fields = this.getClass().getDeclaredFields();
        Object[] keys = outputParams.keySet().toArray();
        for (int i = 0; i < keys.length; i++)
        {
            boolean geoParam = false;
            String data = (String)keys[i];
            for (int j = 0; j < _textGeoParameters.length; j++)
            {
                if (_textGeoParameters[j].getData().equals(data))
                {
                    _textGeoParameters[j].setSelection(Integer.parseInt((String)outputParams.get(data)));
                    geoParam = true;
                    break;
                }
            }

            if (geoParam)
                continue;

            for (int j = 0; j < fields.length; j++)
            {
                Control control;
                Object object = fields[j].get(this);
                if (object instanceof Control)
                    control = (Control)object;
                else
                    continue;

                if (data.equals((String)control.getData()))
                {
                    if (control instanceof Combo)
                    {
                        Combo combo = (Combo)control;
                        for (int k = 0; k < combo.getItemCount(); k++)
                            if (combo.getItem(k).equals((String)outputParams.get(data)))
                                combo.select(k);
                    }
                    else if (control instanceof Spinner)
                        ((Spinner)control).setSelection(Integer.parseInt((String)outputParams.get(data)));
                }
            }
        }
    }

    protected TreeMap getParams() throws IllegalArgumentException, IllegalAccessException
    {
        TreeMap params = new TreeMap();

        for (int i = 0; i < _textGeoParameters.length; i++)
        {
            String data = (String)_textGeoParameters[i].getData();
            if (data != null)
                params.put(data, String.valueOf(_textGeoParameters[i].getSelection()));
        }

        Field[] fields = this.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++)
        {
            Control control;
            Object object = fields[i].get(this);
            if (object instanceof Control)
                control = (Control)object;
            else
                continue;

            String data = (String)control.getData();
            if (data != null)
            {
                if (control instanceof Combo)
                    params.put(data, ((Combo)control).getText());
                else if (control instanceof Spinner)
                    params.put(data, String.valueOf(((Spinner)control).getSelection()));
            }
        }

        return params;
    }

	protected void SetFocus()
    {
		switch(tabFolderControls.getSelectionIndex()) {
		case 0:
			//comboDetailShapeType.forceFocus();
			comboDetailLeadIn.forceFocus();
			break;
		case 1:
			//comboHolesShapeType.forceFocus();
			comboHolesLeadIn.forceFocus();
			break;
		case 2:
			if(macrosParametersCount > 0)
				_textGeoParameters[0].forceFocus();
			break;
		default:
		}
	}
}


