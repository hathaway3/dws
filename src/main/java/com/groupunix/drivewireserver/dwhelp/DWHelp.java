package com.groupunix.drivewireserver.dwhelp;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.groupunix.drivewireserver.dwcommands.DWCmd;
import com.groupunix.drivewireserver.dwcommands.DWCommand;
import com.groupunix.drivewireserver.dwexceptions.DWHelpTopicNotFoundException;
import com.groupunix.drivewireserver.dwprotocolhandler.DWProtocol;

public class DWHelp {
	private XMLConfiguration help;
	private String helpfile = null;

	private static final Logger logger = Logger.getLogger("DWHelp");

	public DWHelp(String helpfile) {
		this.helpfile = helpfile;

		this.reload();
	}

	public DWHelp(DWProtocol dwProto) {
		// try
		// {
		help = new XMLConfiguration();
		addAllTopics(new DWCmd(dwProto), "");
		// help.setFileName(DWDefs.HELP_DEFAULT_FILE);
		// help.save(DWDefs.HELP_DEFAULT_FILE);
		// }
		// catch (ConfigurationException e)
		// {
		// logger.warn(e.getMessage());
		// }

	}

	public void reload() {
		// load helpfile if possible
		logger.debug("reading help from '" + this.helpfile + "'");

		try {

			this.help = new XMLConfiguration(helpfile);

			// local help file

			this.help.setListDelimiter((char) 10);

			// this.help.setAutoSave(true);

		} catch (ConfigurationException e1) {
			logger.warn("Error loading help file: " + e1.getMessage());
		}

	}

	public boolean hasTopic(String topic) {
		if (this.help != null)
			return (this.help.containsKey("topics." + this.spaceToDot(topic) + ".text"));
		return false;
	}

	public String getTopicText(String topic) throws DWHelpTopicNotFoundException {
		if (this.hasTopic(topic)) {
			String text = new String();

			topic = this.spaceToDot(topic);

			String[] txts = help.getStringArray("topics." + topic + ".text");

			for (int i = 0; i < txts.length; i++) {
				text += txts[i] + "\r\n";
			}

			return (text);
		} else {
			throw new DWHelpTopicNotFoundException("There is no help available for the topic '" + topic + "'.");
		}
	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getTopics(String topic) {
		ArrayList<String> res = new ArrayList<String>();

		if (this.help != null) {
			for (Iterator<String> itk = help.configurationAt("topics").getKeys(); itk.hasNext();) {
				String key = itk.next();
				if (key.endsWith(".text"))
					res.add(this.dotToSpace(key.substring(0, key.length() - 5)));

			}
		}

		return (res);
	}

	private String spaceToDot(String topic) {
		return topic.replaceAll(" ", "\\.");
	}

	private String dotToSpace(String topic) {
		return topic.replaceAll("\\.", " ");
	}

	private void addAllTopics(DWCommand dwc, String prefix) {
		String key = "topics.";

		if (!prefix.equals(""))
			key += spaceToDot(prefix) + ".";

		key += spaceToDot(dwc.getCommand()) + ".text";

		help.addProperty(key, dwc.getUsage());
		help.addProperty(key, "");
		help.addProperty(key, dwc.getShortHelp());

		if (dwc.getCommandList() != null) {
			for (DWCommand dwsc : dwc.getCommandList().getCommands()) {
				if (!prefix.equals(""))
					addAllTopics(dwsc, prefix + " " + dwc.getCommand());
				else
					addAllTopics(dwsc, dwc.getCommand());
			}
		}

	}

	@SuppressWarnings("unchecked")
	public ArrayList<String> getSectionTopics(String section) {
		ArrayList<String> res = new ArrayList<String>();
		if (this.help != null) {
			for (Iterator<String> itk = help.configurationAt("topics." + section).getKeys(); itk.hasNext();) {
				String key = itk.next();
				if (key.endsWith(".text"))
					res.add(this.dotToSpace(key.substring(0, key.length() - 5)));

			}
		}
		return (res);
	}

}
