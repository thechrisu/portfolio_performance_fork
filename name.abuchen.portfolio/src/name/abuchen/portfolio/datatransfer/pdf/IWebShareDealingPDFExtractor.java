package name.abuchen.portfolio.datatransfer.pdf;

import static name.abuchen.portfolio.util.TextUtil.trim;

import java.math.BigDecimal;

import name.abuchen.portfolio.datatransfer.ExtractorUtils;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.Block;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.DocumentType;
import name.abuchen.portfolio.datatransfer.pdf.PDFParser.Transaction;
import name.abuchen.portfolio.model.AccountTransaction;
import name.abuchen.portfolio.model.BuySellEntry;
import name.abuchen.portfolio.model.Client;
import name.abuchen.portfolio.model.PortfolioTransaction;
import name.abuchen.portfolio.money.Values;

/**
 * @formatter:off
 * @implNote IWeb Share Dealing is a UK-based financial services company operated by Halifax Share Dealing Limited.
 *           The currency is GBP --> £.
 *
 * @implSpec All security currencies are GBP --> £.
 * @formatter:on
 */

@SuppressWarnings("nls")
public class IWebShareDealingPDFExtractor extends AbstractPDFExtractor
{
    public IWebShareDealingPDFExtractor(Client client)
    {
        super(client);

        addBankIdentifier("IWeb Share Dealing");
        addBankIdentifier("Halifax Share Dealing Limited");

        addBuySellTransaction();
        addDepositTransaction();
    }

    @Override
    public String getLabel()
    {
        return "IWeb Share Dealing";
    }

    private void addBuySellTransaction()
    {
        DocumentType type = new DocumentType("CASH MOVEMENTS SCHEDULE");
        this.addDocumentTyp(type);

        Transaction<BuySellEntry> pdfTransaction = new Transaction<>();

        Block firstRelevantLine = new Block("^[\\d]{2}\\/[\\d]{2}\\/[\\d]{4} Purchase of .*$");
        type.addBlock(firstRelevantLine);
        firstRelevantLine.set(pdfTransaction);

        pdfTransaction //

                        .subject(() -> {
                            BuySellEntry portfolioTransaction = new BuySellEntry();
                            portfolioTransaction.setType(PortfolioTransaction.Type.BUY);
                            return portfolioTransaction;
                        })

                        // @formatter:off
                        // 29/08/2024 Purchase of 7650.32 UNITED KINGDOMGOVERNMENT OF
                        // 0.25 BDS 31/01/25 GBP1000
                        // Deal date: 27/08/2024
                        // Bargain Reference: XXXXXXXX
                        // 7500.00 0.00
                        // @formatter:on
                        .section("date", "shares", "name", "name1") //
                        .match("^(?<date>[\\d]{2}\\/[\\d]{2}\\/[\\d]{4}) Purchase of (?<shares>[\\.,\\d]+) (?<name>.*)$") //
                        .match("^(?<name1>.*)$") //
                        .match("^Deal date:.*$") //
                        .assign((t, v) -> {
                            t.setDate(asDate(v.get("date")));
                            t.setShares(asShares(v.get("shares")));

                            // Combine name lines if name1 doesn't start with "Deal date"
                            if (!v.get("name1").startsWith("Deal date"))
                                v.put("name", v.get("name") + " " + trim(v.get("name1")));

                            v.put("currency", "GBP");
                            t.setSecurity(getOrCreateSecurity(v));
                        })

                        // @formatter:off
                        // Bargain Reference: XXXXXXXX
                        // @formatter:on
                        .section("note").optional() //
                        .match("^Bargain Reference: (?<note>.*)$") //
                        .assign((t, v) -> t.setNote("Ref: " + trim(v.get("note"))))

                        // @formatter:off
                        // 7500.00 0.00
                        // @formatter:on
                        .section("amount") //
                        .match("^Bargain Reference:.*$") //
                        .match("^(?<amount>[\\.,\\d]+) [\\.,\\d]+.*$") //
                        .assign((t, v) -> {
                            t.setCurrencyCode("GBP");
                            t.setAmount(asAmount(v.get("amount")));
                        })

                        .wrap(BuySellEntryItem::new);
    }

    private void addDepositTransaction()
    {
        DocumentType type = new DocumentType("CASH MOVEMENTS SCHEDULE");
        this.addDocumentTyp(type);

        Transaction<AccountTransaction> pdfTransaction = new Transaction<>();

        Block firstRelevantLine = new Block("^[\\d]{2}\\/[\\d]{2}\\/[\\d]{4} Payment received .*$");
        type.addBlock(firstRelevantLine);
        firstRelevantLine.set(pdfTransaction);

        pdfTransaction //

                        .subject(() -> {
                            AccountTransaction accountTransaction = new AccountTransaction();
                            accountTransaction.setType(AccountTransaction.Type.DEPOSIT);
                            return accountTransaction;
                        })

                        // @formatter:off
                        // 27/08/2024 Payment received 7500.00 7500.00cr
                        // @formatter:on
                        .section("date", "amount") //
                        .match("^(?<date>[\\d]{2}\\/[\\d]{2}\\/[\\d]{4}) Payment received (?<amount>[\\.,\\d]+) [\\.,\\d]+cr$") //
                        .assign((t, v) -> {
                            t.setDateTime(asDate(v.get("date")));
                            t.setCurrencyCode("GBP");
                            t.setAmount(asAmount(v.get("amount")));
                        })

                        .wrap(TransactionItem::new);
    }

    @Override
    protected long asAmount(String value)
    {
        return ExtractorUtils.convertToNumberLong(value, Values.Amount, "en", "UK");
    }

    @Override
    protected long asShares(String value)
    {
        return ExtractorUtils.convertToNumberLong(value, Values.Share, "en", "UK");
    }

    @Override
    protected BigDecimal asExchangeRate(String value)
    {
        return ExtractorUtils.convertToNumberBigDecimal(value, Values.Share, "en", "UK");
    }
}
