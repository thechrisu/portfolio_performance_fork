package name.abuchen.portfolio.datatransfer.pdf.iwebsharedealing;

import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.deposit;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasAmount;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasCurrencyCode;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasDate;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasName;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasNote;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasShares;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.hasSource;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.purchase;
import static name.abuchen.portfolio.datatransfer.ExtractorMatchers.security;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countAccountTransactions;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countBuySell;
import static name.abuchen.portfolio.datatransfer.ExtractorTestUtilities.countSecurities;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import name.abuchen.portfolio.datatransfer.actions.AssertImportActions;
import name.abuchen.portfolio.datatransfer.pdf.IWebShareDealingPDFExtractor;
import name.abuchen.portfolio.datatransfer.pdf.PDFInputFile;
import name.abuchen.portfolio.model.Client;

@SuppressWarnings("nls")
public class IWebShareDealingPDFExtractorTest
{
    @Test
    public void testBuy01()
    {
        var extractor = new IWebShareDealingPDFExtractor(new Client());

        List<Exception> errors = new ArrayList<>();

        var results = extractor.extract(PDFInputFile.loadTestCase(getClass(), "Buy01.txt"), errors);

        assertThat(errors, empty());
        assertThat(countSecurities(results), is(1L));
        assertThat(countBuySell(results), is(3L));
        assertThat(countAccountTransactions(results), is(3L));
        assertThat(results.size(), is(7));
        new AssertImportActions().check(results, "GBP");

        // check security
        assertThat(results, hasItem(security( //
                        hasName("UNITED KINGDOMGOVERNMENT OF 0.25 BDS 31/01/25 GBP1000"), //
                        hasCurrencyCode("GBP"))));

        // check first deposit
        assertThat(results, hasItem(deposit( //
                        hasDate("2024-08-27"), //
                        hasAmount("GBP", 7500.00), //
                        hasSource("Buy01.txt"))));

        // check first purchase
        assertThat(results, hasItem(purchase( //
                        hasDate("2024-08-29"), hasShares(7650.32), //
                        hasSource("Buy01.txt"), //
                        hasNote("Ref: XXXXXXXX"), //
                        hasAmount("GBP", 7500.00))));

        // check second deposit
        assertThat(results, hasItem(deposit( //
                        hasDate("2024-08-29"), //
                        hasAmount("GBP", 7500.00), //
                        hasSource("Buy01.txt"))));

        // check third deposit
        assertThat(results, hasItem(deposit( //
                        hasDate("2024-08-30"), //
                        hasAmount("GBP", 7500.00), //
                        hasSource("Buy01.txt"))));

        // check second purchase
        assertThat(results, hasItem(purchase( //
                        hasDate("2024-09-03"), hasShares(7648.00), //
                        hasSource("Buy01.txt"), //
                        hasNote("Ref: YYYYYYYY"), //
                        hasAmount("GBP", 7498.42))));

        // check third purchase
        assertThat(results, hasItem(purchase( //
                        hasDate("2024-09-03"), hasShares(7646.87), //
                        hasSource("Buy01.txt"), //
                        hasNote("Ref: ZZZZZZZZ"), //
                        hasAmount("GBP", 7500.00))));
    }
}
