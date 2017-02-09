package com.panchong.test;

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

	// Ҫץȡ�ı����ܳ��ֵ�������
	// static String[] Propertys={"��ҵ����","ע���/ͳһ������ô���","�����뾭Ӫ�쳣��¼����"};
	static String[] Propertys = { "�ؿ���", "�ڵر��", "�ؿ�λ��", "�õ�����", "�滮���",
			"�������", "����ʱ��", "������ʼ��", "λ��", "����ʱ��", "���", "�滮��;", "�ݻ���", "���",
			"�ɽ���", "���׷�ʽ", "������" };

	// ȡ�������table����isValueElement��������ǲ���������Ҫ��table
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

	// �����ȡ����Ϣ�����ϵĵ�λ��һЩ�ַ����ƥ������ƶ�
	public static String adjuestmentParm(String parm) {
		if (regual("\\(", parm)) {
			parm = parm.substring(0, parm.indexOf("("));
		} else if (regual("��", parm)) {
			parm = parm.substring(0, parm.indexOf("��"));
		} else if (regual("��Ԫ", parm)) {
			parm = parm.substring(0, parm.indexOf("��Ԫ"));
		} else if (regual("Ķ", parm)) {
			parm = parm.substring(0, parm.indexOf("Ķ"));
		} else if (regual("����", parm)) {
			parm = parm.substring(0, parm.indexOf("����"));
		} else if (regual("��", parm)) {
			parm = parm.substring(0, parm.indexOf("��"));
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