<?xml version="1.0" encoding="utf-8"?>
<Request>
	<ClientNO>${jyxxsq.kpddm!}</ClientNO>
	<SerialNumber>${jyxxsq.jylsh!}</SerialNumber>
	<InvType>12</InvType>
	<Spbmbbh>13.0</Spbmbbh>
	<Drawer>${jyxxsq.kpr!}</Drawer>
	<Payee>${jyxxsq.skr!}</Payee>
	<Reviewer>${jyxxsq.fhr!}</Reviewer>
    <DataSource>${jyxxsq.sjly!}</DataSource>
    <OpenId>${jyxxsq.openid!}</OpenId>
	<Seller>
		<Identifier>${jyxxsq.xfsh!}</Identifier>
		<Name>${jyxxsq.xfmc!}</Name>
		<Address>${jyxxsq.xfdz!}</Address>
		<TelephoneNo>${jyxxsq.xfdh!}</TelephoneNo>
		<Bank>${jyxxsq.xfyh!}</Bank>
		<BankAcc>${jyxxsq.xfyhzh!}</BankAcc>
	</Seller>
	<OrderSize count="1">
		<Order>
			<OrderMain>
				<OrderNo>${jyxxsq.ddh!}</OrderNo>
				<InvoiceList>0</InvoiceList>
				<InvoiceSplit>1</InvoiceSplit>
				<InvoiceSfdy>0</InvoiceSfdy>
				<OrderDate>${jylssj!}</OrderDate>
				<ChargeTaxWay>0</ChargeTaxWay>
				<TotalAmount>${jyxxsq.jshj?c!}</TotalAmount>
				<TotalDiscount><#if (jyxxsq.qjzk)??>${jyxxsq.qjzk?c!}<#else></#if></TotalDiscount>
				<TaxMark>${jyxxsq.hsbz!}</TaxMark>
				<Remark>${jyxxsq.bz!}</Remark>
				<ExtractedCode>${jyxxsq.tqm!}</ExtractedCode>
				<Buyer>
					<CustomerType>${jyxxsq.gflx!}</CustomerType>
					<Identifier>${jyxxsq.gfsh!}</Identifier>
					<Name><![CDATA[${jyxxsq.gfmc!}]]></Name>
					<Address>${jyxxsq.gfdz!}</Address>
					<TelephoneNo>${jyxxsq.gfdh!}</TelephoneNo>
					<Bank>${jyxxsq.gfyh!}</Bank>
					<BankAcc>${jyxxsq.gfyhzh!}</BankAcc>
					<Email>${jyxxsq.gfemail!}</Email>
					<IsSend>${jyxxsq.sffsyj!}</IsSend>
					<Recipient>${jyxxsq.gfsjr!}</Recipient>
					<ReciAddress></ReciAddress>
					<Zip>${jyxxsq.gfyb!}</Zip>
                    <MobilephoneNo>${jyxxsq.gfsjh!}</MobilephoneNo>
				</Buyer>
			</OrderMain>
			<OrderDetails count="${count}">
				 <#list jymxsqList as jymxsq>
				<ProductItem>
					<VenderOwnCode></VenderOwnCode>
					<ProductCode>${jymxsq.spdm!}</ProductCode>
					<ProductName><![CDATA[${jymxsq.spmc!}]]></ProductName>
					<RowType>${jymxsq.fphxz!}</RowType>
					<Spec>${jymxsq.spggxh!}</Spec>
					<Unit>${jymxsq.spdw!}</Unit>
					<Quantity>
						<#if (jymxsq.sps)??>
						${jymxsq.sps?c!}
						<#else>
						</#if>
					</Quantity>
					<UnitPrice>
						<#if (jymxsq.spdj)??>
						${jymxsq.spdj?c!}
						<#else>
						</#if>
					</UnitPrice>
					<Amount>${jymxsq.spje?c!}</Amount>
					<DeductAmount></DeductAmount>
					<TaxRate>${jymxsq.spsl?c!}</TaxRate>
					<TaxAmount>
						<#if (jymxsq.spse)??>
						${jymxsq.spse?c!}
						<#else>
						</#if>
					</TaxAmount>
					<MxTotalAmount>${jymxsq.jshj?c!}</MxTotalAmount>
					<PolicyMark>
						<#if (jymxsq.yhzcbs)??>
							<#if jymxsq.yhzcbs!="">
							${jymxsq.yhzcbs!}
							<#else>
                                0
							</#if>
						<#else>
						${jymxsq.yhzcbs!"0"}
						</#if>
					</PolicyMark>
					<TaxRateMark>${jymxsq.lslbz!}</TaxRateMark>
					<PolicyName>${jymxsq.yhzcmc!}</PolicyName>
				</ProductItem>
				</#list>
			</OrderDetails>
            <Payments>
                <#list jyzfmxList as jyzfmx>
                <PaymentItem>
                    <PayCode>${jyzfmx.zffsDm!}</PayCode>
                    <PayPrice>${jyzfmx.zfje?c!}</PayPrice>
                </PaymentItem>
				</#list>
            </Payments>
		</Order>
	</OrderSize>
</Request>
