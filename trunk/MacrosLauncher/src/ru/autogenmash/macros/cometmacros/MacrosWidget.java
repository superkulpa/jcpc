/*$Id: MacrosWidget.java,v 1.4 2012/11/19 06:59:27 Kulpanov Exp $*/
package ru.autogenmash.macros.cometmacros;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.autogenmash.gui.AGMStyledSWTForm;
import ru.autogenmash.macros.cometmacros.JMacros;

/**
 * @author Dymarchuk Dmitry
 * @version
 * refactor 27.08.2007 14:45:13
 */
public class MacrosWidget
{
	  public static final String FIELD_NAME = "NAME";

    public static final String DEFAULT_DETAIL_NAME = "Деталь";

    public static final int TEXT_HEIGHT = 25;

    /** Ширина подсвечиваемой области (в пикселах). */
  	public static final int DRIFT = 5;
  
  	protected Composite _composite;
  	protected Label _labelImage;
    protected Label _labelText;
  	protected String _macrosName;
    protected String _description;


    public MacrosWidget(Composite parent, String macrosName, int size, Image image)
    {
	    _macrosName = macrosName;
      _description = readDescription();

  		_composite = new Composite(parent, SWT.NONE);
  		_composite.setSize(size, size);
  		_composite.setBackground(AGMStyledSWTForm.COLOR_BLUE);
      _composite.setLayout(new FormLayout());

  		_labelImage = new Label(_composite, SWT.CENTER);
  		_labelImage.setBackground(AGMStyledSWTForm.COLOR_BLUE);
  		 image.setBackground(_composite.getBackground());
  		_labelImage.setImage(image);
  		_labelImage.setSize(size - 2 * DRIFT, size - 2 * DRIFT - TEXT_HEIGHT);
  		_labelImage.setLocation(DRIFT, DRIFT);
  		_labelImage.setData("parent", this);

      _labelText = new Label(_composite, SWT.CENTER);
      _labelText.setBackground(AGMStyledSWTForm.COLOR_BLUE);
      _labelText.setSize(size - 2 * DRIFT, TEXT_HEIGHT);
      _labelText.setLocation(DRIFT, size - DRIFT - TEXT_HEIGHT);
      _labelText.setFont(AGMStyledSWTForm.FONT_12B);
      _labelText.setText(_description);
	}

    public void addMouseListener(MouseListener listener)
    {
        _labelImage.addMouseListener(listener);
    }
    
    public void removeMouseListener(MouseListener listener)
    {
        _labelImage.removeMouseListener(listener);
    }
    
    public void loadMacros()
    {
        try
        {
            JMacros macros = null;
            Class macrosClass = Class.forName(_macrosName);
            Object macrosObject = macrosClass.newInstance( );
            if (macrosObject instanceof JMacros)
            {
                macros = (JMacros)macrosObject;
                macros.Init();
                macros.OpenForm();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected String readDescription()
    {
        String res = "";
        try
        {
            Class macrosClass = Class.forName(_macrosName);
            res = (String)macrosClass.getField(FIELD_NAME).get(null);
        }
        catch (Exception e)
        {
            if (e instanceof NoSuchFieldException)
                res = DEFAULT_DETAIL_NAME;
            else
                e.printStackTrace();
        }
        return res;
    }

//	public void loadMacros()
//    {
//		String execStr = "";
//		if(MacrosLauncher.OS_NAME.equalsIgnoreCase(MacrosLauncher.OS_WIN))
//			execStr = "JAVA -classpath ru.autogenmash.macros.cometmacros/BaseClasses.jar;ru.autogenmash.macros.cometmacros/BaseMacros.jar;SWT/swt.jar -Djava.library.path=SWT ru.autogenmash.macros.cometmacros." + _macrosName;
//		else
//			execStr = "./j9/bin/j9 -jit -classpath ./ru.autogenmash.macros.cometmacros/BaseClasses.jar:./ru.autogenmash.macros.cometmacros/BaseMacros.jar:./SWT/swt.jar -nojit -Djava.library.path=./SWT ru.autogenmash.macros.cometmacros." + _macrosName;
//		try{
//			Process p = Runtime.getRuntime().exec(execStr);
//			p.waitFor();
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	public void setBackground(Color color)
    {
		_composite.setBackground(color);
	}

	public void setFocus()
    {
		_composite.setFocus();
		_labelImage.setFocus();
	}

	public void setLocation(int x, int y)
    {
		_composite.setLocation(x, y);
	}

    public String getMacrosName()
    {
        return _macrosName;
    }

    public String getDescription()
    {
        return _description;
    }
}
