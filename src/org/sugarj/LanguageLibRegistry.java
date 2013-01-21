package org.sugarj;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.sugarj.common.Log;

/**
 * Registry for active language libraries. A language library should register here
 * on activation. The editor and compiler query this registry to receive language
 * libraries.
 * 
 * @author seba
 */
public class LanguageLibRegistry {

  private boolean extensionsLoaded = false;
  
  private Map<String, LanguageLibFactory> languageLibs = new HashMap<String, LanguageLibFactory>();

  private static LanguageLibRegistry instance = new LanguageLibRegistry();

  private LanguageLibRegistry() {
  }

  public static LanguageLibRegistry getInstance() {
    return instance;
  }

  public synchronized void registerLanguageLib(LanguageLibFactory libFactory) {
    languageLibs.put(libFactory.getSugarFileExtension(), libFactory);
    if (libFactory.getOriginalFileExtension() != null)
      languageLibs.put(libFactory.getOriginalFileExtension(), libFactory);
  }

  public synchronized void unregisterLanguageLib(LanguageLibFactory libFactory) {
    LanguageLibFactory reg = languageLibs.get(libFactory.getSugarFileExtension());
    if (reg != null && reg.equals(libFactory))
      languageLibs.remove(libFactory.getSugarFileExtension());
    
    if (libFactory.getOriginalFileExtension() != null) {
      reg = languageLibs.get(libFactory.getOriginalFileExtension());
      if (reg != null && reg.equals(libFactory))
        languageLibs.remove(libFactory.getOriginalFileExtension());
    }
  }
  
  public synchronized LanguageLibFactory getLanguageLib(String extension) {
    if (!extensionsLoaded)
      loadExtensions();
    
    return languageLibs.get(extension);
  }
  
  public synchronized boolean isRegistered(String extension) {
    if (!extensionsLoaded)
      loadExtensions();

    return languageLibs.containsKey(extension);
  }
  
  private synchronized void loadExtensions() {
    extensionsLoaded = true;

    if (!Platform.isRunning())
      return;
   
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("org.sugarj.language");
    IConfigurationElement[] members = extensionPoint.getConfigurationElements();
    for (IConfigurationElement activatorConf : members) {
      String pluginId = activatorConf.getAttribute("id");
      loadLanguagePlugin(pluginId);
    }
  }
  
  public synchronized Bundle loadLanguagePlugin(String pluginId) {
    Bundle bundle = Platform.getBundle(pluginId);
    if (bundle == null)
      Log.log.logErr("Could not load language plugin " + pluginId, Log.ALWAYS);
    else
      try {
        bundle.start();
      } catch (BundleException e) {
        Log.log.logErr("Could not start language plugin " + pluginId, Log.ALWAYS);
      }
    return bundle;
  }
  
  public synchronized List<String> getRegisteredFileExtensions() {
    if (!extensionsLoaded)
      loadExtensions();

    List<String> list = new LinkedList<String>();
    for (Entry<String, LanguageLibFactory> entry : languageLibs.entrySet())
      if (entry.getKey().equals(entry.getValue().getSugarFileExtension()))
        list.add(entry.getValue().getSugarFileExtension());
    return list;
  }
}
