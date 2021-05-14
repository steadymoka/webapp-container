import SwiftUI
import Combine
import WebKit

let WEB_APP_BRIDGE = "Bridge"

extension WebView.Coordinator: WKScriptMessageHandler {
    
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        if message.name != WEB_APP_BRIDGE { return }
        guard let json : [String: Any] = message.body as? Dictionary else { print("invalid format"); return }
        guard let handler = json["handler"] as? String else { print("invalid format handler"); return }
        guard let args = json["args"] as? Array<Any> else { print("invalid format args"); return }
        let cid = json["cid"] as? Int ?? 0
        
        let viewModel = parent.viewModel
        
        // switch handler
        if (handler == WebViewModel.onSplashVisible) {
            viewModel.onSplashVisible.send(args[safe: 0] as? Bool ?? true)
            if (0 != cid) {
                viewModel.callback.send(cid)
            }
        }
        if (handler == WebViewModel.onLogout) {
            viewModel.onLogout.send(true)
            if (0 != cid) {
                viewModel.callback.send(cid)
            }
        }
        if (handler == WebViewModel.clearHistory) {
            viewModel.clearHistory.send(args[safe: 0] as? String ?? "")
            if (0 != cid) {
                viewModel.callback.send(cid)
            }
        }
    }
    
    func webView(
        _ webView: WKWebView,
        decidePolicyFor navigationAction: WKNavigationAction,
        decisionHandler: @escaping (WKNavigationActionPolicy) -> Void
    ) {
        self.parent.viewModel.callback.sink(receiveValue: { cid in
            webView.evaluateJavaScript("__webkitCallback(\(cid))", completionHandler: nil)
        }).store(in: &cancellables)
            
        self.parent.viewModel.requestRoutePush.sink(receiveValue: { link in
            webView.evaluateJavaScript("__vueRoutePush('\(link)')", completionHandler: nil)
        }).store(in: &cancellables)
        return decisionHandler(.allow)
    }
}

struct WebView: UIViewRepresentable {

    var url: String
    
    @ObservedObject var viewModel: WebViewModel
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    func makeUIView(context: Context) -> WKWebView {
        let preferences = WKPreferences()
        preferences.javaScriptCanOpenWindowsAutomatically = false
        
        let configuration = WKWebViewConfiguration()
        configuration.preferences = preferences
        configuration.userContentController.add(self.makeCoordinator(), name: WEB_APP_BRIDGE)
        
        let webView = WKWebView(frame: CGRect.zero, configuration: configuration)
        webView.navigationDelegate = context.coordinator
        webView.allowsBackForwardNavigationGestures = false
        webView.scrollView.isScrollEnabled = true
        
        if let url = URL(string: url) {
            webView.load(URLRequest(url: url))
        }
        return webView
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {}
    
    ///
    
    class Coordinator : NSObject, WKNavigationDelegate {
        
        var parent: WebView
        
        private var cancellables = Set<AnyCancellable>()
        
        init(_ uiWebView: WebView) {
            self.parent = uiWebView
        }

        deinit {
            cancellables.forEach { (cancellable) in
                cancellable.cancel()
            }
        }
        
        var start: Double = 0
        
        func webView(_ webView: WKWebView, didStartProvisionalNavigation navigation: WKNavigation!) {
            print("기본 프레임에서 탐색이 시작되었음 -->> \(floor(Date().timeIntervalSince1970 * 1000))")
        }
        
        func webView(_ webView: WKWebView, didCommit navigation: WKNavigation!) {
            start = floor(Date().timeIntervalSince1970 * 1000)
            print("내용을 수신하기 시작 -->> \(start)")
        }
        
        func webView(_ webview: WKWebView, didFinish: WKNavigation!) {
            let end = floor(Date().timeIntervalSince1970 * 1000)
            let duration = end - start
            print("탐색이 완료 -->> \(start) || duration: \(duration) ms")
        }
        
        func webView(
            _ webView: WKWebView,
            didFailProvisionalNavigation: WKNavigation!,
            withError: Error) {
            print("초기 탐색 프로세스 중에 오류가 발생했음")
        }
        
        func webView(
            _ webView: WKWebView,
            didFail navigation: WKNavigation!,
            withError error: Error) {
            print("탐색 중에 오류가 발생했음")
        }
    }

}
