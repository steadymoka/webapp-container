import UIKit
import Firebase

class AppDelegate: UIResponder, UIApplicationDelegate, MessagingDelegate, UNUserNotificationCenterDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        return true

        // -- 
        // Firebase
        FirebaseApp.configure()
        
        // Firebase Cloud Message
        Messaging.messaging().delegate = self
        Messaging.messaging().token { token, error in
          if let error = error {
            print("Error fetching FCM registration token: \(error)")
          } else if let token = token {
            print("FCM registration token: \(token)")
            UserDefaults.standard.set(token, forKey: "Key.fcmToken")
          }
        }
        
        // Notification
        UNUserNotificationCenter.current().delegate = self
        UNUserNotificationCenter
            .current()
            .requestAuthorization(options: [.alert, .badge, .sound],
                completionHandler: { _, _ in }
            )
        
        application.registerForRemoteNotifications()
        return true
    }
    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if (nil != fcmToken) {
            UserDefaults.standard.set(fcmToken, forKey: "Key.fcmToken")
        }
        messaging.subscribe(toTopic: "general")
    }

    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.alert, .badge, .sound])
    }

    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        completionHandler()
    }
    
}
