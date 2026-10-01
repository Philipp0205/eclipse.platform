/*******************************************************************************
 * Copyright (c) 2007, 2020 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.terminal.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.terminal.connector.Logger;

/**
 * @author scharf
 * Flyweight
 * Threadsafe.
 * @since 5.0
 *
 */
// TODO add an Object for user data, use weak map to keep track of styles with associated
// user data
public class TerminalStyle {
	private final TerminalColor fForegroundTerminalColor;
	private final TerminalColor fBackgroundTerminalColor;
	private final RGB fForegroundRGB;
	private final RGB fBackgroundRGB;
	private final boolean fBold;
	private final boolean fBlink;
	private final boolean fUnderline;
	private final boolean fReverse;
	private final int fExtra;
	private static final int EXTRA_DIM = 1;
	private static final int EXTRA_ITALIC = 2;
	private static final int EXTRA_STRIKE = 4;
	private final static Map<TerminalStyle, TerminalStyle> fgStyles = Collections
			.synchronizedMap(new LinkedHashMap<TerminalStyle, TerminalStyle>() {
				@Override
				protected boolean removeEldestEntry(Map.Entry<TerminalStyle, TerminalStyle> eldest) {
					int size = size();
					boolean removeEldest = size >= 1000;
					if (Platform.getDebugBoolean(Logger.TRACE_DEBUG_LOG_VT100BACKEND)) {
						if (removeEldest) {
							Logger.log("Removing eldest Style from style cache, size = " + size); //$NON-NLS-1$
						} else {
							Logger.log("Leaving eldest Style in style cache, size = " + size); //$NON-NLS-1$
						}
					}
					return removeEldest;
				}
			});

	private TerminalStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor,
			RGB foregroundRGB, RGB backgroundRGB, boolean bold, boolean blink, boolean underline, boolean reverse,
			int extra) {
		Assert.isLegal(foregroundTerminalColor == null || foregroundRGB == null,
				"Only one of ANSI or RGB colors can be specified as a foreground color"); //$NON-NLS-1$
		Assert.isLegal(backgroundTerminalColor == null || backgroundRGB == null,
				"Only one of ANSI or RGB colors can be specified as a background color"); //$NON-NLS-1$
		fForegroundTerminalColor = foregroundTerminalColor;
		fBackgroundTerminalColor = backgroundTerminalColor;
		fForegroundRGB = foregroundRGB;
		fBackgroundRGB = backgroundRGB;
		fBold = bold;
		fBlink = blink;
		fUnderline = underline;
		fReverse = reverse;
		fExtra = extra;
	}

	public static TerminalStyle getStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor,
			RGB foregroundRGB, RGB backgroundRGB, boolean bold, boolean blink, boolean underline, boolean reverse) {
		return getStyle(foregroundTerminalColor, backgroundTerminalColor, foregroundRGB, backgroundRGB, bold, blink,
				underline, reverse, 0);
	}

	private static TerminalStyle getStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor,
			RGB foregroundRGB, RGB backgroundRGB, boolean bold, boolean blink, boolean underline, boolean reverse,
			int extra) {
		TerminalStyle style = new TerminalStyle(foregroundTerminalColor, backgroundTerminalColor, foregroundRGB,
				backgroundRGB, bold, blink, underline, reverse, extra);
		// If set had a computeIfAbsent we would use a set, instead just store 1-2-1 mapping
		return fgStyles.computeIfAbsent(style, (s) -> style);
	}

	public static TerminalStyle getStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor,
			boolean bold, boolean blink, boolean underline, boolean reverse) {
		return getStyle(foregroundTerminalColor, backgroundTerminalColor, null, null, bold, blink, underline, reverse);
	}

	public static TerminalStyle getStyle(RGB foregroundRGB, RGB backgroundRGB, boolean bold, boolean blink,
			boolean underline, boolean reverse) {
		return getStyle(null, null, foregroundRGB, backgroundRGB, bold, blink, underline, reverse);
	}

	public static TerminalStyle getDefaultStyle() {
		return getStyle(TerminalColor.FOREGROUND, TerminalColor.BACKGROUND);
	}

	public static TerminalStyle getStyle(TerminalColor foregroundTerminalColor, TerminalColor backgroundTerminalColor) {
		return getStyle(foregroundTerminalColor, backgroundTerminalColor, null, null, false, false, false, false);
	}

	private TerminalStyle derive(TerminalColor fg, TerminalColor bg, RGB fgRGB, RGB bgRGB, boolean bold,
			boolean blink, boolean underline, boolean reverse) {
		return getStyle(fg, bg, fgRGB, bgRGB, bold, blink, underline, reverse, fExtra);
	}

	private TerminalStyle withExtra(int mask, boolean on) {
		int extra = on ? (fExtra | mask) : (fExtra & ~mask);
		return getStyle(fForegroundTerminalColor, fBackgroundTerminalColor, fForegroundRGB, fBackgroundRGB, fBold,
				fBlink, fUnderline, fReverse, extra);
	}

	/**
	 * @since 5.1
	 */
	public TerminalStyle setDim(boolean dim) {
		return withExtra(EXTRA_DIM, dim);
	}

	/**
	 * @since 5.1
	 */
	public TerminalStyle setItalic(boolean italic) {
		return withExtra(EXTRA_ITALIC, italic);
	}

	/**
	 * @since 5.1
	 */
	public TerminalStyle setStrikethrough(boolean strikethrough) {
		return withExtra(EXTRA_STRIKE, strikethrough);
	}

	/**
	 * @since 5.1
	 */
	public boolean isDim() {
		return (fExtra & EXTRA_DIM) != 0;
	}

	/**
	 * @since 5.1
	 */
	public boolean isItalic() {
		return (fExtra & EXTRA_ITALIC) != 0;
	}

	/**
	 * @since 5.1
	 */
	public boolean isStrikethrough() {
		return (fExtra & EXTRA_STRIKE) != 0;
	}

	public TerminalStyle setForeground(TerminalColor foregroundTerminalColor) {
		return derive(foregroundTerminalColor, fBackgroundTerminalColor, null, fBackgroundRGB, fBold, fBlink,
				fUnderline, fReverse);
	}

	public TerminalStyle setBackground(TerminalColor backgroundTerminalColor) {
		return derive(fForegroundTerminalColor, backgroundTerminalColor, fForegroundRGB, null, fBold, fBlink,
				fUnderline, fReverse);
	}

	public TerminalStyle setForeground(RGB foregroundRGB) {
		return derive(null, fBackgroundTerminalColor, foregroundRGB, fBackgroundRGB, fBold, fBlink, fUnderline,
				fReverse);
	}

	public TerminalStyle setBackground(RGB backgroundRGB) {
		return derive(fForegroundTerminalColor, null, fForegroundRGB, backgroundRGB, fBold, fBlink, fUnderline,
				fReverse);
	}

	public TerminalStyle setForeground(TerminalStyle other) {
		return derive(other.fForegroundTerminalColor, fBackgroundTerminalColor, other.fForegroundRGB, fBackgroundRGB,
				fBold, fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setBackground(TerminalStyle other) {
		return derive(fForegroundTerminalColor, other.fBackgroundTerminalColor, fForegroundRGB, other.fBackgroundRGB,
				fBold, fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setForeground(int eightBitindexedColor) {
		boolean isIndexTerminalColor = TerminalColor.isIndexedTerminalColor(eightBitindexedColor);
		if (isIndexTerminalColor) {
			TerminalColor foregroundTerminalColor = TerminalColor.getIndexedTerminalColor(eightBitindexedColor);
			return derive(foregroundTerminalColor, fBackgroundTerminalColor, null, fBackgroundRGB, fBold, fBlink,
					fUnderline, fReverse);
		} else {
			RGB foregroundRGB = TerminalColor.getIndexedRGBColor(eightBitindexedColor);
			return derive(null, fBackgroundTerminalColor, foregroundRGB, fBackgroundRGB, fBold, fBlink, fUnderline,
					fReverse);
		}
	}

	public TerminalStyle setBackground(int eightBitindexedColor) {
		boolean isIndexTerminalColor = TerminalColor.isIndexedTerminalColor(eightBitindexedColor);
		if (isIndexTerminalColor) {
			TerminalColor backgroundTerminalColor = TerminalColor.getIndexedTerminalColor(eightBitindexedColor);
			return derive(fForegroundTerminalColor, backgroundTerminalColor, fForegroundRGB, null, fBold, fBlink,
					fUnderline, fReverse);
		} else {
			RGB backgroundRGB = TerminalColor.getIndexedRGBColor(eightBitindexedColor);
			return derive(fForegroundTerminalColor, null, fForegroundRGB, backgroundRGB, fBold, fBlink, fUnderline,
					fReverse);
		}
	}

	public TerminalStyle setBold(boolean bold) {
		return derive(fForegroundTerminalColor, fBackgroundTerminalColor, fForegroundRGB, fBackgroundRGB, bold,
				fBlink, fUnderline, fReverse);
	}

	public TerminalStyle setBlink(boolean blink) {
		return derive(fForegroundTerminalColor, fBackgroundTerminalColor, fForegroundRGB, fBackgroundRGB, fBold,
				blink, fUnderline, fReverse);
	}

	public TerminalStyle setUnderline(boolean underline) {
		return derive(fForegroundTerminalColor, fBackgroundTerminalColor, fForegroundRGB, fBackgroundRGB, fBold,
				fBlink, underline, fReverse);
	}

	public TerminalStyle setReverse(boolean reverse) {
		return derive(fForegroundTerminalColor, fBackgroundTerminalColor, fForegroundRGB, fBackgroundRGB, fBold,
				fBlink, fUnderline, reverse);
	}

	public TerminalColor getForegroundTerminalColor() {
		return fForegroundTerminalColor;
	}

	public TerminalColor getBackgroundTerminalColor() {
		return fBackgroundTerminalColor;
	}

	public RGB getForegroundRGB() {
		return fForegroundRGB;
	}

	public RGB getBackgroundRGB() {
		return fBackgroundRGB;
	}

	public boolean isBlink() {
		return fBlink;
	}

	public boolean isBold() {
		return fBold;
	}

	public boolean isReverse() {
		return fReverse;
	}

	public boolean isUnderline() {
		return fUnderline;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fBackgroundTerminalColor == null) ? 0 : fBackgroundTerminalColor.hashCode());
		result = prime * result + ((fBackgroundRGB == null) ? 0 : fBackgroundRGB.hashCode());
		result = prime * result + (fBlink ? 1231 : 1237);
		result = prime * result + (fBold ? 1231 : 1237);
		result = prime * result + ((fForegroundTerminalColor == null) ? 0 : fForegroundTerminalColor.hashCode());
		result = prime * result + ((fForegroundRGB == null) ? 0 : fForegroundRGB.hashCode());
		result = prime * result + (fReverse ? 1231 : 1237);
		result = prime * result + (fUnderline ? 1231 : 1237);
		result = prime * result + fExtra;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TerminalStyle other = (TerminalStyle) obj;
		if (fBackgroundTerminalColor != other.fBackgroundTerminalColor) {
			return false;
		}
		if (fBackgroundRGB == null) {
			if (other.fBackgroundRGB != null) {
				return false;
			}
		} else if (!fBackgroundRGB.equals(other.fBackgroundRGB)) {
			return false;
		}
		if (fBlink != other.fBlink) {
			return false;
		}
		if (fBold != other.fBold) {
			return false;
		}
		if (fForegroundTerminalColor != other.fForegroundTerminalColor) {
			return false;
		}
		if (fForegroundRGB == null) {
			if (other.fForegroundRGB != null) {
				return false;
			}
		} else if (!fForegroundRGB.equals(other.fForegroundRGB)) {
			return false;
		}
		if (fReverse != other.fReverse) {
			return false;
		}
		if (fUnderline != other.fUnderline) {
			return false;
		}
		if (fExtra != other.fExtra) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("Style(foreground="); //$NON-NLS-1$
		if (fForegroundTerminalColor != null) {
			result.append(fForegroundTerminalColor);
		} else {
			result.append(fForegroundRGB);
		}
		result.append(", background="); //$NON-NLS-1$
		if (fForegroundTerminalColor != null) {
			result.append(fBackgroundTerminalColor);
		} else {
			result.append(fBackgroundRGB);
		}
		if (fBlink) {
			result.append(", blink"); //$NON-NLS-1$
		}
		if (fBold) {
			result.append(", bold"); //$NON-NLS-1$
		}
		if (fBlink) {
			result.append(", blink"); //$NON-NLS-1$
		}
		if (fReverse) {
			result.append(", reverse"); //$NON-NLS-1$
		}
		if (fUnderline) {
			result.append(", underline"); //$NON-NLS-1$
		}
		if (isDim()) {
			result.append(", dim"); //$NON-NLS-1$
		}
		if (isItalic()) {
			result.append(", italic"); //$NON-NLS-1$
		}
		if (isStrikethrough()) {
			result.append(", strikethrough"); //$NON-NLS-1$
		}
		result.append(")"); //$NON-NLS-1$
		return result.toString();
	}

}
