package org.csstudio.opibuilder.widgets.model;

import org.csstudio.opibuilder.model.AbstractPVWidgetModel;
import org.csstudio.opibuilder.properties.BooleanProperty;
import org.csstudio.opibuilder.properties.DoubleProperty;
import org.csstudio.opibuilder.properties.WidgetPropertyCategory;

/**
 * This class defines a common widget model for any widget 
 * which has a scale. 
 * @author Xihui Chen
 */
public abstract class AbstractScaledWidgetModel extends AbstractPVWidgetModel {
	
	
	/** The ID of the <i>transparent</i> property. */
	public static final String PROP_TRANSPARENT = "transparent_background";	
	
	/** The ID of the minimum property. */
	public static final String PROP_MIN = "minimum"; //$NON-NLS-1$		
	
	/** The ID of the maximum property. */
	public static final String PROP_MAX = "maximum"; //$NON-NLS-1$		
	
	/** The ID of the major tick step hint property. */
	public static final String PROP_MAJOR_TICK_STEP_HINT = "major_tick_step_hint"; //$NON-NLS-1$		
	
	/** The ID of the show minor ticks property. */
	public static final String PROP_SHOW_MINOR_TICKS = "show_minor_ticks"; //$NON-NLS-1$
	
	/** The ID of the show minor ticks property. */
	public static final String PROP_SHOW_SCALE = "show_scale"; //$NON-NLS-1$
	
	/** The ID of the log scale property. */
	public static final String PROP_LOG_SCALE = "log_scale"; //$NON-NLS-1$	
	
	
	/** The default value of the minimum property. */
	private static final double DEFAULT_MIN = 0;
	
	/** The default value of the maximum property. */
	private static final double DEFAULT_MAX = 100;	
	
		/** The default value of the major tick step hint property. */
	private static final double DEFAULT_MAJOR_TICK_STEP_HINT = 50;	

	@Override
	protected void configureProperties() {
		
		addProperty(new BooleanProperty(PROP_TRANSPARENT, "Transparent Background",
				WidgetPropertyCategory.Display, true));
		
		
		addProperty(new DoubleProperty(PROP_MIN, "Minimum", 
				WidgetPropertyCategory.Behavior, DEFAULT_MIN));
		
		addProperty(new DoubleProperty(PROP_MAX, "Maximum", 
				WidgetPropertyCategory.Behavior, DEFAULT_MAX));			
		
		addProperty(new DoubleProperty(PROP_MAJOR_TICK_STEP_HINT, "Major Tick Step Hint", 
				WidgetPropertyCategory.Display, DEFAULT_MAJOR_TICK_STEP_HINT, 1, 1000));			
		
		addProperty(new BooleanProperty(PROP_SHOW_MINOR_TICKS, "Show Minor Ticks", 
				WidgetPropertyCategory.Display, true));		
		
		addProperty(new BooleanProperty(PROP_SHOW_SCALE, "Show Scale", 
				WidgetPropertyCategory.Display, true));		
		
		addProperty(new BooleanProperty(PROP_LOG_SCALE, "Log Scale", 
				WidgetPropertyCategory.Display, false));
		
	}
	


	/**
	 * @return the minimum value
	 */
	public Double getMinimum() {
		return (Double) getProperty(PROP_MIN).getPropertyValue();
	}


	/**
	 * @return the maximum value
	 */
	public Double getMaximum() {
		return (Double) getProperty(PROP_MAX).getPropertyValue();
	}

	/**
	 * @return the major tick step hint value
	 */
	public Double getMajorTickStepHint() {
		return (Double) getProperty(PROP_MAJOR_TICK_STEP_HINT).getPropertyValue();
	}

	
	
	/**
	 * @return true if the minor ticks should be shown, false otherwise
	 */
	public boolean isShowMinorTicks() {
		return (Boolean) getProperty(PROP_SHOW_MINOR_TICKS).getPropertyValue();
	}
	
	/**
	 * @return true if the scale should be shown, false otherwise
	 */
	public boolean isShowScale() {
		return (Boolean) getProperty(PROP_SHOW_SCALE).getPropertyValue();
	}
	

	/**
	 * @return true if log scale enabled, false otherwise
	 */
	public boolean isLogScaleEnabled() {
		return (Boolean) getProperty(PROP_LOG_SCALE).getPropertyValue();
	}
	
	/**
	 * Returns, if this widget should have a transparent background.
	 * @return boolean
	 * 				True, if it should have a transparent background, false otherwise
	 */
	public boolean isTransparent() {
		return (Boolean) getProperty(PROP_TRANSPARENT).getPropertyValue();
	}
	
}
