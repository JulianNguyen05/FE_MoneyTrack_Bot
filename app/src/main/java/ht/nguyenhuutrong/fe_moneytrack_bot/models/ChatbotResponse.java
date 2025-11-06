package ht.nguyenhuutrong.fe_moneytrack_bot.models;

// (Import thư viện Gson nếu bạn dùng)
// import com.google.gson.annotations.SerializedName;

public class ChatbotResponse {

    // @SerializedName("reply") // Dùng nếu tên biến khác với JSON
    private String reply;

    public String getReply() {
        return reply;
    }

    // (getter/setter nếu cần)
}