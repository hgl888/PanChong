package com.pachong.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

public class DataTableUtil {

	private final static int NUM = 5;

	// 要抓取的表格可能出现的属性名
	// static String[] Propertys={"企业名称","注册号/统一社会信用代码","被列入经营异常名录日期"};
	static String[] Propertys = { "地块编号", "宗地编号", "地块位置", "用地性质", "规划面积",
			"出让面积", "发布时间", "挂牌起始价", "位置", "交易时间", "面积", "规划用途", "容积率", "起价",
			"成交价", "交易方式", "竞得人" };

	// 取最里面的table进入isValueElement方法检测是不是我们需要的table
	public static List<TableElement> getFitElement(Document document) {
		if (Propertys != null) {
			Element element = document.getElementsByTag("body").get(0);
			List<TableElement> fitElments = new ArrayList<TableElement>();
			Elements tableElements = element.getElementsByTag("table");
			if (tableElements != null && tableElements.size() > 0) {
				for (int i = 0; i < tableElements.size(); i++) {
					Element tableElement = tableElements.get(i);
					Elements ces = tableElement.getElementsByTag("table");
					if (ces != null && ces.size() > 1) {
					} else {
						TableElement te;
						if ((te = isValueElement(Propertys, tableElement)) != null) {
							fitElments.add(te);
						}
					}
				}
			} else {
				return null;
			}
			return fitElments;
		}
		return null;
	}

	private static Element removeRedundance(String[] Propertys, Element element) {
		Elements tres = element.getElementsByTag("tr");
		Element trElement = tres.get(0);
		Elements tde = trElement.getElementsByTag("td");
		int row = 1;
		for (Element tdElement : tde) {
			String attribute = tdElement.attr("rowspan");
			if (attribute != null && !attribute.equals("")) {
				int rowSpan = Integer.valueOf(attribute);
				if (rowSpan > row) {
					row = rowSpan;
				}
			}
		}
		List<Element> elements = new ArrayList<Element>();
		for (int i = 0; i < row; i++) {
			elements.add(tres.get(i));
		}
		int ri = 0;
		while (!isValueElements(Propertys, elements)) {
			elements = new ArrayList<Element>();
			row = 1;
			Elements tdes = tres.get(ri).getElementsByTag("td");
			for (Element tdElement : tdes) {
				String attribute = tdElement.attr("rowspan");
				if (attribute != null && !attribute.equals("")) {
					int rowSpan = Integer.valueOf(attribute);
					if (rowSpan > row) {
						row = rowSpan;
					}
				}
			}
			for (int i = 0; i < row; i++) {
				elements.add(tres.get(ri + i));
			}
			ri = ri + row;
		}
		if (ri > 0) {
			Elements trs = element.getElementsByTag("tr");
			int size = trs.size();
			Element newElement = new Element(Tag.valueOf("table"), "table");
			for (int i = ri - row; i < size; i++) {
				newElement.appendChild(trs.get(i));
			}
			return newElement;
		}
		return element;
	}

	private static boolean isValueElements(String[] Propertys,
										   List<Element> trElements) {
		int index = 0;
		int size = trElements.size();
		for (int i = 0; i < size; i++) {
			List<Element> propertyElements = new ArrayList<Element>();
			Element element = trElements.get(i);
			Elements tdElements = element.getElementsByTag("td");
			Elements thElements = element.getElementsByTag("th");
			if (thElements != null && thElements.size() > 0) {
				for (Element thelement : thElements) {
					propertyElements.add(thelement);
				}
			}
			if (tdElements != null && tdElements.size() > 0) {
				for (Element tdelement : tdElements) {
					propertyElements.add(tdelement);
				}
			}
			for (Element tdElement : propertyElements) {
				String text = tdElement.text();
				if (!text.trim().equals("")) {
					String value = adjuestmentParm(text);
					if (value != null) {
						value = StringUtil.parseString(value);
						double max = 0.0d;
						for (int j = 0; j < Propertys.length; j++) {
							double temp = SimFeatureUtil.sim(Propertys[j],
									value);
							if (temp > max) {
								max = temp;
							}
						}
						if (max >= 0.6) {
							index++;
						}
					}
				}
			}
		}
		if (index >= NUM) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean regual(String reg, String string) {
		Pattern pattern = Pattern.compile(reg);
		Matcher ma = pattern.matcher(string);
		if (ma.find()) {
			return true;
		}
		return false;
	}

	// 清除获取的信息标题上的单位及一些字符提高匹配的相似度
	public static String adjuestmentParm(String parm) {
		if (regual("\\(", parm)) {
			parm = parm.substring(0, parm.indexOf("("));
		} else if (regual("（", parm)) {
			parm = parm.substring(0, parm.indexOf("（"));
		} else if (regual("万元", parm)) {
			parm = parm.substring(0, parm.indexOf("万元"));
		} else if (regual("亩", parm)) {
			parm = parm.substring(0, parm.indexOf("亩"));
		} else if (regual("公顷", parm)) {
			parm = parm.substring(0, parm.indexOf("公顷"));
		} else if (regual("米", parm)) {
			parm = parm.substring(0, parm.indexOf("米"));
		}
		return parm;
	}

	private static TableElement isValueElement(String[] Propertys,
											   Element element) {
		TableElement tableElement = new TableElement();
		List<Element> propertyElements = new ArrayList<Element>();
		Elements tdElements = element.getElementsByTag("td");
		Elements thElements = element.getElementsByTag("th");
		if (thElements != null && thElements.size() > 0) {
			for (Element thelement : thElements) {
				propertyElements.add(thelement);
			}
		}
		if (tdElements != null && tdElements.size() > 0) {
			for (Element tdelement : tdElements) {
				propertyElements.add(tdelement);
			}
		}
		int index = 0;
		int consist = 0;
		int size = propertyElements.size();
		int consistFlag = 0;
		for (int i = 0; i < size; i++) {
			consist++;
			Element tdElement = propertyElements.get(i);
			String text = tdElement.text();
			if (!text.trim().equals("")) {
				String value = adjuestmentParm(text);
				if (value != null) {
					value = StringUtil.parseString(value);
					double max = 0.0d;
					for (int j = 0; j < Propertys.length; j++) {
						double temp = SimFeatureUtil.sim(Propertys[j], value);
						if (temp > max) {
							max = temp;
						}
					}
					if (max >= 0.6) {
						index++;
						if (consist == 1) {
							consist = 0;
							consistFlag++;
						} else {
							consist = 0;
						}
					} else {
						if (consist >= 10) {
							break;
						}
					}
				}
			}
		}
		if (index >= NUM) {
			tableElement.setWordNum(index);
			if (consistFlag >= 2) {
				tableElement.setElement(removeRedundance(Propertys, element));
				tableElement.setCross(false);
			} else {
				tableElement.setElement(element);
			}
			return tableElement;
		} else {
			return null;
		}
	}
}