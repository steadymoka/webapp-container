import SwiftUI
import Combine

class WebViewModel: ObservableObject {
    
    /// call function WEB to APP
    var callback = PassthroughSubject<Int, Never>()
    
    static var onSplashVisible = "onSplashVisible"
    var onSplashVisible = PassthroughSubject<Bool, Never>()
    
    static var onLogout = "onLogout"
    var onLogout = PassthroughSubject<Bool, Never>()
    
    static var clearHistory = "clearHistory"
    var clearHistory = PassthroughSubject<String, Never>()
    
    /// call function APP to WEB
    var requestRoutePush = PassthroughSubject<String, Never>()
}

struct MainView: View {
    
    @ObservedObject var viewModel = WebViewModel()
    
    @State private var splash: Bool = true
    
    private let fcmToken = UserDefaults.standard.string(forKey: "Key.fcmToken") ?? ""
    
    var body: some View {
        GeometryReader { geometry in
            ZStack(alignment: .center) {
                WebView(url: "http://192.168.31.24:3000/app/iOS?fcmToken=\(fcmToken)", viewModel: viewModel)
                    .frame(width: geometry.size.width, height: geometry.size.height)
                    .opacity((splash) ? 0 : 1)
            
                ZStack(alignment: .center) {
                    Spacer()
                        .background(Color.white)
                        .frame(width: geometry.size.width, height: geometry.size.height)

                    if (splash == true) {
                        VStack(alignment: .center, spacing: 8) {
                            Spacer()
                            Text("로그인/회원가입")
                                .font(.system(size: 16, weight: .medium))
                                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: 56)
                                .foregroundColor(Color.white)
                                .background(Color(hex: 0xFF6200EE))
                                .cornerRadius(24)
                                .padding(.horizontal, 32)
                                .onTapGesture {
                                    viewModel.requestRoutePush.send("/auth")
                                }
                            Text("시작하기")
                                .font(.system(size: 16, weight: .medium))
                                .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: 56)
                                .foregroundColor(Color(hex: 0xFF6200EE))
                                .background(Color.white)
                                .overlay(RoundedRectangle(cornerRadius: 24).stroke(Color(hex: 0xFF6200EE)))
                                .padding(.horizontal, 32)
                                .onTapGesture {
                                    viewModel.requestRoutePush.send("/app")
                                }
                            Spacer()
                        }
                        .frame(width: geometry.size.width, height: geometry.size.height)
                    }
                }
                .frame(width: geometry.size.width, height: geometry.size.height)
                .opacity((splash) ? 1 : 0)
            }
        }
        .edgesIgnoringSafeArea(.vertical)
        .onReceive(self.viewModel.onSplashVisible.receive(on: RunLoop.main)) { visible in
            withAnimation() {
                splash = visible
            }
        }
        .onReceive(self.viewModel.clearHistory.receive(on: RunLoop.main)) { link in
            viewModel.requestRoutePush.send(link)
        }
        .onReceive(self.viewModel.onLogout.receive(on: RunLoop.main)) { visible in
            viewModel.requestRoutePush.send("/app/iOS?fcmToken=\(fcmToken)")
            
            withAnimation {
                splash = true
            }
        }
    }
}
