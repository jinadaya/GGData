import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinInitializerKt.doInitKoin(clientId: IgdbSecrets.clientId, clientSecret: IgdbSecrets.clientSecret)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}
