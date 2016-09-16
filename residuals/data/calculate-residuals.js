const fs = require('fs');
const d3 = require('d3');
const _ = require('lodash');
const csvWriter = require('csv-write-stream');

const rossman20kConfig = {
  algos: ['dl', 'drf',  'gbm', 'glm'],
  project: 'rossman-store-sales',
  predictColumn: 'predict',
  responseColumn: 'Sales',
  numericColumns: ['Customers', 'CompetitionDistance'],
  fileStem: '-combined-validation-predict',
  fileSuffix: '-20k'
}

const rossmanConfig = {
  algos: ['dl', 'drf',  'gbm', 'glm'],
  project: 'rossman-store-sales',
  predictColumn: 'predict',
  responseColumn: 'Sales',
  numericColumns: ['Customers', 'CompetitionDistance'],
  fileStem: '-combined-validation-predict',
  fileSuffix: ''
}

const walmart20kConfig = {
  algos: ['dl'], //, 'drf',  'gbm', 'glm'],
  project: 'walmart-trip-type',
  predictColumn: 'predict',
  responseColumn: 'TripType',
  numericColumn: ['ScanCount'],
  residualType: 'pearson',
  fileStem: '-combined-validation-predict',
  fileSuffix: '-20k'
}

const adultConfig = {
  project: 'adult',
  residualType: 'pearsonAdvisor',
  fileStem: 'Logit',
  fileSuffix: ''
}

const santanderConfig = {
  algos: ['dl', 'drf',  'gbm', 'glm'],
  project: 'santander-customer-satisfaction',
  predictColumn: 'predict',
  responseColumn: 'TARGET',
  numericColumns: ["predict","ID","var3","var15","imp_ent_var16_ult1","imp_op_var39_comer_ult1","imp_op_var39_comer_ult3","imp_op_var40_comer_ult1","imp_op_var40_comer_ult3","imp_op_var40_efect_ult1","imp_op_var40_efect_ult3","imp_op_var40_ult1","imp_op_var41_comer_ult1","imp_op_var41_comer_ult3","imp_op_var41_efect_ult1","imp_op_var41_efect_ult3","imp_op_var41_ult1","imp_op_var39_efect_ult1","imp_op_var39_efect_ult3","imp_op_var39_ult1","imp_sal_var16_ult1","ind_var1_0","ind_var1","ind_var2_0","ind_var2","ind_var5_0","ind_var5","ind_var6_0","ind_var6","ind_var8_0","ind_var8","ind_var12_0","ind_var12","ind_var13_0","ind_var13_corto_0","ind_var13_corto","ind_var13_largo_0","ind_var13_largo","ind_var13_medio_0","ind_var13_medio","ind_var13","ind_var14_0","ind_var14","ind_var17_0","ind_var17","ind_var18_0","ind_var18","ind_var19","ind_var20_0","ind_var20","ind_var24_0","ind_var24","ind_var25_cte","ind_var26_0","ind_var26_cte","ind_var26","ind_var25_0","ind_var25","ind_var27_0","ind_var28_0","ind_var28","ind_var27","ind_var29_0","ind_var29","ind_var30_0","ind_var30","ind_var31_0","ind_var31","ind_var32_cte","ind_var32_0","ind_var32","ind_var33_0","ind_var33","ind_var34_0","ind_var34","ind_var37_cte","ind_var37_0","ind_var37","ind_var39_0","ind_var40_0","ind_var40","ind_var41_0","ind_var41","ind_var39","ind_var44_0","ind_var44","ind_var46_0","ind_var46","num_var1_0","num_var1","num_var4","num_var5_0","num_var5","num_var6_0","num_var6","num_var8_0","num_var8","num_var12_0","num_var12","num_var13_0","num_var13_corto_0","num_var13_corto","num_var13_largo_0","num_var13_largo","num_var13_medio_0","num_var13_medio","num_var13","num_var14_0","num_var14","num_var17_0","num_var17","num_var18_0","num_var18","num_var20_0","num_var20","num_var24_0","num_var24","num_var26_0","num_var26","num_var25_0","num_var25","num_op_var40_hace2","num_op_var40_hace3","num_op_var40_ult1","num_op_var40_ult3","num_op_var41_hace2","num_op_var41_hace3","num_op_var41_ult1","num_op_var41_ult3","num_op_var39_hace2","num_op_var39_hace3","num_op_var39_ult1","num_op_var39_ult3","num_var27_0","num_var28_0","num_var28","num_var27","num_var29_0","num_var29","num_var30_0","num_var30","num_var31_0","num_var31","num_var32_0","num_var32","num_var33_0","num_var33","num_var34_0","num_var34","num_var35","num_var37_med_ult2","num_var37_0","num_var37","num_var39_0","num_var40_0","num_var40","num_var41_0","num_var41","num_var39","num_var42_0","num_var42","num_var44_0","num_var44","num_var46_0","num_var46","saldo_var1","saldo_var5","saldo_var6","saldo_var8","saldo_var12","saldo_var13_corto","saldo_var13_largo","saldo_var13_medio","saldo_var13","saldo_var14","saldo_var17","saldo_var18","saldo_var20","saldo_var24","saldo_var26","saldo_var25","saldo_var28","saldo_var27","saldo_var29","saldo_var30","saldo_var31","saldo_var32","saldo_var33","saldo_var34","saldo_var37","saldo_var40","saldo_var41","saldo_var42","saldo_var44","saldo_var46","var36","delta_imp_amort_var18_1y3","delta_imp_amort_var34_1y3","delta_imp_aport_var13_1y3","delta_imp_aport_var17_1y3","delta_imp_aport_var33_1y3","delta_imp_compra_var44_1y3","delta_imp_reemb_var13_1y3","delta_imp_reemb_var17_1y3","delta_imp_reemb_var33_1y3","delta_imp_trasp_var17_in_1y3","delta_imp_trasp_var17_out_1y3","delta_imp_trasp_var33_in_1y3","delta_imp_trasp_var33_out_1y3","delta_imp_venta_var44_1y3","delta_num_aport_var13_1y3","delta_num_aport_var17_1y3","delta_num_aport_var33_1y3","delta_num_compra_var44_1y3","delta_num_reemb_var13_1y3","delta_num_reemb_var17_1y3","delta_num_reemb_var33_1y3","delta_num_trasp_var17_in_1y3","delta_num_trasp_var17_out_1y3","delta_num_trasp_var33_in_1y3","delta_num_trasp_var33_out_1y3","delta_num_venta_var44_1y3","imp_amort_var18_hace3","imp_amort_var18_ult1","imp_amort_var34_hace3","imp_amort_var34_ult1","imp_aport_var13_hace3","imp_aport_var13_ult1","imp_aport_var17_hace3","imp_aport_var17_ult1","imp_aport_var33_hace3","imp_aport_var33_ult1","imp_var7_emit_ult1","imp_var7_recib_ult1","imp_compra_var44_hace3","imp_compra_var44_ult1","imp_reemb_var13_hace3","imp_reemb_var13_ult1","imp_reemb_var17_hace3","imp_reemb_var17_ult1","imp_reemb_var33_hace3","imp_reemb_var33_ult1","imp_var43_emit_ult1","imp_trans_var37_ult1","imp_trasp_var17_in_hace3","imp_trasp_var17_in_ult1","imp_trasp_var17_out_hace3","imp_trasp_var17_out_ult1","imp_trasp_var33_in_hace3","imp_trasp_var33_in_ult1","imp_trasp_var33_out_hace3","imp_trasp_var33_out_ult1","imp_venta_var44_hace3","imp_venta_var44_ult1","ind_var7_emit_ult1","ind_var7_recib_ult1","ind_var10_ult1","ind_var10cte_ult1","ind_var9_cte_ult1","ind_var9_ult1","ind_var43_emit_ult1","ind_var43_recib_ult1","var21","num_var2_0_ult1","num_var2_ult1","num_aport_var13_hace3","num_aport_var13_ult1","num_aport_var17_hace3","num_aport_var17_ult1","num_aport_var33_hace3","num_aport_var33_ult1","num_var7_emit_ult1","num_var7_recib_ult1","num_compra_var44_hace3","num_compra_var44_ult1","num_ent_var16_ult1","num_var22_hace2","num_var22_hace3","num_var22_ult1","num_var22_ult3","num_med_var22_ult3","num_med_var45_ult3","num_meses_var5_ult3","num_meses_var8_ult3","num_meses_var12_ult3","num_meses_var13_corto_ult3","num_meses_var13_largo_ult3","num_meses_var13_medio_ult3","num_meses_var17_ult3","num_meses_var29_ult3","num_meses_var33_ult3","num_meses_var39_vig_ult3","num_meses_var44_ult3","num_op_var39_comer_ult1","num_op_var39_comer_ult3","num_op_var40_comer_ult1","num_op_var40_comer_ult3","num_op_var40_efect_ult1","num_op_var40_efect_ult3","num_op_var41_comer_ult1","num_op_var41_comer_ult3","num_op_var41_efect_ult1","num_op_var41_efect_ult3","num_op_var39_efect_ult1","num_op_var39_efect_ult3","num_reemb_var13_hace3","num_reemb_var13_ult1","num_reemb_var17_hace3","num_reemb_var17_ult1","num_reemb_var33_hace3","num_reemb_var33_ult1","num_sal_var16_ult1","num_var43_emit_ult1","num_var43_recib_ult1","num_trasp_var11_ult1","num_trasp_var17_in_hace3","num_trasp_var17_in_ult1","num_trasp_var17_out_hace3","num_trasp_var17_out_ult1","num_trasp_var33_in_hace3","num_trasp_var33_in_ult1","num_trasp_var33_out_hace3","num_trasp_var33_out_ult1","num_venta_var44_hace3","num_venta_var44_ult1","num_var45_hace2","num_var45_hace3","num_var45_ult1","num_var45_ult3","saldo_var2_ult1","saldo_medio_var5_hace2","saldo_medio_var5_hace3","saldo_medio_var5_ult1","saldo_medio_var5_ult3","saldo_medio_var8_hace2","saldo_medio_var8_hace3","saldo_medio_var8_ult1","saldo_medio_var8_ult3","saldo_medio_var12_hace2","saldo_medio_var12_hace3","saldo_medio_var12_ult1","saldo_medio_var12_ult3","saldo_medio_var13_corto_hace2","saldo_medio_var13_corto_hace3","saldo_medio_var13_corto_ult1","saldo_medio_var13_corto_ult3","saldo_medio_var13_largo_hace2","saldo_medio_var13_largo_hace3","saldo_medio_var13_largo_ult1","saldo_medio_var13_largo_ult3","saldo_medio_var13_medio_hace2","saldo_medio_var13_medio_hace3","saldo_medio_var13_medio_ult1","saldo_medio_var13_medio_ult3","saldo_medio_var17_hace2","saldo_medio_var17_hace3","saldo_medio_var17_ult1","saldo_medio_var17_ult3","saldo_medio_var29_hace2","saldo_medio_var29_hace3","saldo_medio_var29_ult1","saldo_medio_var29_ult3","saldo_medio_var33_hace2","saldo_medio_var33_hace3","saldo_medio_var33_ult1","saldo_medio_var33_ult3","saldo_medio_var44_hace2","saldo_medio_var44_hace3","saldo_medio_var44_ult1","saldo_medio_var44_ult3","var38","TARGET"],
  fileStem: '-combined-validation-predict',
  fileSuffix: ''
}

function calculateResiduals(config) {
  const algos = config.algos;
  const predictColumn = config.predictColumn;
  const responseColumn = config.responseColumn;
  const project = config.project;
  const fileSuffix = config.fileSuffix;
  const fileStem = config.fileStem;
  const residualType = config.residualType;
  const algo = algos[3];

  const inputPath = `${project}/input`;
  const outputPath = `${project}/output`;
  const csvfile1 = `${inputPath}/${algo}${fileStem}${fileSuffix}.csv`;
  const data = d3.csv.parse(fs.readFileSync(csvfile1, 'utf8'));

  const categories = d3.set(data.map(d => d[responseColumn]))
    .values()
    .sort(); 

  console.log('unique categories from responseColumn', categories);

  data.forEach((d, i) => {
    const predicted = Number(d[predictColumn]);
    const actual = Number(d[responseColumn]);
    const actualString = d[responseColumn];

    let residual;
    if (residualType === 'pearson') {
      const estimate = Number(d[`p${actual}`]); // p40 posterior value etc
      const currentCategoryIndex = categories.indexOf(actualString); // '40' the category string
      const numberOfCategories = categories.length;
      const observed = numberOfCategories - currentCategoryIndex;

      console.log('predicted', predicted);
      console.log('actual', actual);
      console.log('estimate', estimate);
      console.log('currentCategoryIndex', currentCategoryIndex);
      console.log('numberOfCategories', numberOfCategories);
      console.log('observed', observed);

      residual = (observed - estimate) / (Math.sqrt(estimate * (1 - estimate)));
    } else if (residualType === 'pearsonAdvisor') {
      const estimate = Number(d.Estimates);
      const observed = Number(d.Observed);

      console.log('estimate', estimate);
      console.log('observed', observed);

      residual = (observed - estimate) / (Math.sqrt(estimate * (1 - estimate)));
    } else {
      residual = actual - predicted;
    }
    d.residual = residual;
  })
  let outputData = data;

  // write a csv file
  var writer = csvWriter();
  writer.pipe(fs.createWriteStream(`${outputPath}/${algo}-residuals${fileSuffix}.csv`));
  outputData.forEach(d => {
      writer.write(d);
  })
  writer.end();

}

calculateResiduals(santanderConfig);
