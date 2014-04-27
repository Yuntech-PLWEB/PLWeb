package org.plweb.jedit;

import java.awt.BorderLayout;
import java.awt.Desktop;

import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAdapter;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserWindowWillOpenEvent;
import chrriis.dj.nativeswing.swtimpl.components.WebBrowserNavigationEvent;

public class BrowserUserInterface extends JPanel {

	private static final long serialVersionUID = -7815610502515979763L;
	private ProjectEnvironment env = ProjectEnvironment.getInstance();

	public BrowserUserInterface() {
		setLayout(new BorderLayout());
		// JPanel panel = new JPanel();

		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				combineBrowserUserInfo(), createDisplayBox());
		pane.setOneTouchExpandable(true);
		pane.setDividerLocation(400);

		add(pane);
	}

	private JComponent combineBrowserUserInfo(){
		JSplitPane pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createUserInfo(),createBrowser());
		pane.setOneTouchExpandable(false);
		pane.setDividerLocation(120);
		return pane;
	}
	
	private JComponent createUserInfo(){
		JPanel info = new JPanel(new BorderLayout());
		JLabel infoText = new JLabel("<html><h2>系所： " + env.getDepartment() + "<br>學號： " + env.getEnrollment() + "<br>姓名： " + env.getUname() + "</h2></html>");
		info.add(infoText, BorderLayout.WEST);
		
		return info;
	}
	
	private JComponent createBrowser() {
		JWebBrowser browser = new JWebBrowser();
		browser.setBarsVisible(false);
		env.setActiveBrowser(browser);

		browser.setBorder(BorderFactory.createTitledBorder("HTML Viewer"));

		//lintt 20140210 -- open a new default browser when a user clicks a link in JWebBrowser
		browser.addWebBrowserListener(new WebBrowserAdapter() {
    		public void windowWillOpen(WebBrowserWindowWillOpenEvent e) {
        		// get the new swing window
        		final JWebBrowser newBrowser = e.getNewWebBrowser();
        		newBrowser.addWebBrowserListener(new WebBrowserAdapter() {
            		@Override
            		public void locationChanging(WebBrowserNavigationEvent newEvent) {
                		// launch default OS browser
                		if (Desktop.isDesktopSupported()) {
                    		Desktop desktop = Desktop.getDesktop();
                    		if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        		try {
                            		desktop.browse(new URI(newEvent.getNewResourceLocation()));
                        		} catch (Exception ex) {}
                    		}
                		}
                		newEvent.consume();
		                // immediately close the new swing window
        		        SwingUtilities.invokeLater(new Runnable() {
		                    public void run() {
        		                newBrowser.getWebBrowserWindow().dispose();
                		    }
                		});
            		}
        		});
    		}
		});
		//----lintt 20140210
				
		//browser.setHTMLContent("<html><head><meta http-equiv=content-type content=\"text/html; charset=UTF-8\"></head><body></body></html>");
		return browser;
	}

	private JComponent createDisplayBox() {
		JMultiTextViewer viewer = new JMultiTextViewer();
		env.setActiveViewer(viewer);

		return viewer;
	}
}
