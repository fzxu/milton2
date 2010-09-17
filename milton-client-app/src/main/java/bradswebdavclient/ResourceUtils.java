package bradswebdavclient;

import com.ettrema.httpclient.Resource;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author mcevoyb
 */
public class ResourceUtils {
  public static boolean isDeleteKey(int keyChar) {
    return ( keyChar == 127 );
  }
  
  public static void doDelete(Component component, int keyChar, List<Resource> toDelete) {
    if( !isDeleteKey(keyChar) ) return ;
    
    doDelete(component, toDelete);
  }
  
  public static void doDelete(Component component, Resource toDelete) {    
    List<Resource> list = new ArrayList<Resource>();
    list.add(toDelete);
    doDelete(component, list);
  }
  
  public static void doDelete(Component component, List<Resource> toDelete) {    
    if (toDelete.size() == 0) {
      return;
    }

    if (toDelete.size() == 1) {
      Resource r = toDelete.get(0);
      int result = JOptionPane.showConfirmDialog(component, "Are you sure you want to delete: " + r.href() + "?");
      if (result == JOptionPane.YES_OPTION) {
        r.delete();
      } else {
        return;
      }
    } else {
      int result = JOptionPane.showConfirmDialog(component, "Are you sure you want to delete: " + toDelete.size() + " items?");
      if (result == JOptionPane.YES_OPTION) {
        for (Resource r : toDelete) {
          r.delete();
        }
      } else {
        return;
      }
    }
  }
  
}
